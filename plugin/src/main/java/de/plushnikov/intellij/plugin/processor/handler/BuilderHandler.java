package de.plushnikov.intellij.plugin.processor.handler;

import com.intellij.java.language.impl.psi.impl.source.PsiClassReferenceType;
import com.intellij.java.language.psi.*;
import consulo.language.psi.PsiElement;
import consulo.lombok.processor.ProcessorUtil;
import consulo.project.Project;
import consulo.util.collection.ContainerUtil;
import consulo.util.lang.StringUtil;
import de.plushnikov.intellij.plugin.LombokClassNames;
import de.plushnikov.intellij.plugin.lombokconfig.ConfigDiscovery;
import de.plushnikov.intellij.plugin.lombokconfig.LombokNullAnnotationLibrary;
import de.plushnikov.intellij.plugin.problem.ProblemProcessingSink;
import de.plushnikov.intellij.plugin.problem.ProblemSink;
import de.plushnikov.intellij.plugin.processor.JacksonizedProcessor;
import de.plushnikov.intellij.plugin.processor.clazz.ToStringProcessor;
import de.plushnikov.intellij.plugin.processor.clazz.constructor.NoArgsConstructorProcessor;
import de.plushnikov.intellij.plugin.processor.handler.singular.AbstractSingularHandler;
import de.plushnikov.intellij.plugin.processor.handler.singular.SingularHandlerFactory;
import de.plushnikov.intellij.plugin.provider.LombokUserDataKeys;
import de.plushnikov.intellij.plugin.psi.LombokLightClassBuilder;
import de.plushnikov.intellij.plugin.psi.LombokLightMethodBuilder;
import de.plushnikov.intellij.plugin.thirdparty.LombokAddNullAnnotations;
import de.plushnikov.intellij.plugin.thirdparty.LombokCopyableAnnotations;
import de.plushnikov.intellij.plugin.util.LombokProcessorUtil;
import de.plushnikov.intellij.plugin.util.PsiAnnotationSearchUtil;
import de.plushnikov.intellij.plugin.util.PsiAnnotationUtil;
import de.plushnikov.intellij.plugin.util.PsiClassUtil;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static consulo.util.lang.StringUtil.capitalize;
import static consulo.util.lang.StringUtil.replace;
import static de.plushnikov.intellij.plugin.lombokconfig.ConfigKey.BUILDER_CLASS_NAME;

/**
 * Handler methods for Builder-processing
 *
 * @author Tomasz Kalkosiński
 * @author Michail Plushnikov
 */
public class BuilderHandler {
  private final static String ANNOTATION_BUILDER_CLASS_NAME = "builderClassName";
  private static final String ANNOTATION_BUILD_METHOD_NAME = "buildMethodName";
  private static final String ANNOTATION_BUILDER_METHOD_NAME = "builderMethodName";
  public static final String ANNOTATION_SETTER_PREFIX = "setterPrefix";

  private final static String BUILD_METHOD_NAME = "build";
  private final static String BUILDER_METHOD_NAME = "builder";
  public static final String TO_BUILDER_METHOD_NAME = "toBuilder";
  static final String TO_BUILDER_ANNOTATION_KEY = "toBuilder";

  private static final String BUILDER_ANNOTATION_SHORT_NAME = StringUtil.getShortName(LombokClassNames.BUILDER);
  private static final Collection<String> INVALID_ON_BUILDERS = Stream.of(LombokClassNames.GETTER,
                                                                          LombokClassNames.SETTER,
                                                                          LombokClassNames.WITHER,
                                                                          LombokClassNames.WITH,
                                                                          LombokClassNames.TO_STRING,
                                                                          LombokClassNames.EQUALS_AND_HASHCODE,
                                                                          LombokClassNames.REQUIRED_ARGS_CONSTRUCTOR,
                                                                          LombokClassNames.ALL_ARGS_CONSTRUCTOR,
                                                                          LombokClassNames.NO_ARGS_CONSTRUCTOR,
                                                                          LombokClassNames.DATA,
                                                                          LombokClassNames.VALUE,
                                                                          LombokClassNames.FIELD_DEFAULTS)
    .map(StringUtil::getShortName).collect(Collectors.toUnmodifiableSet());
  private static final String JACKSON_DATABIND_ANNOTATION_JSON_POJOBUILDER = "com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder";

  private static final String BUILDER_TEMP_VAR = "builder";

  PsiSubstitutor getBuilderSubstitutor(@Nonnull PsiTypeParameterListOwner classOrMethodToBuild, @Nonnull PsiClass innerClass) {
    PsiSubstitutor substitutor = PsiSubstitutor.EMPTY;
    if (innerClass.hasModifierProperty(PsiModifier.STATIC)) {
      PsiTypeParameter[] typeParameters = classOrMethodToBuild.getTypeParameters();
      PsiTypeParameter[] builderParams = innerClass.getTypeParameters();
      if (typeParameters.length <= builderParams.length) {
        for (int i = 0; i < typeParameters.length; i++) {
          PsiTypeParameter typeParameter = typeParameters[i];
          substitutor = substitutor.put(typeParameter, PsiSubstitutor.EMPTY.substitute(builderParams[i]));
        }
      }
    }
    return substitutor;
  }

  /**
   * Checks if given annotation could be a '@lombok.Builder' annotation.
   * <b>Attention</b>: As a workaround it accepts the annotation immediately,
   * if calculated name for the Builder class is equal to 'Builder' (same as shortName of the lombok annotation),
   * to prevent recursive calls to AugmentProvider.
   * In this case the given annotation is most likely '@Builder' one, because calculation of Builder-Class-Name is based on attributes
   * of the @lombok.Builder annotation!
   * But still can going wrong, if somebody makes global configuration like 'lombok.builder.className=Builder'
   * and used annotation like '@crazy_stuff.Builder'"
   */
  public static boolean checkAnnotationFQN(@Nonnull PsiClass psiClass, @Nonnull PsiAnnotation psiAnnotation, @Nonnull PsiMethod psiMethod) {
    return BUILDER_ANNOTATION_SHORT_NAME.equals(getBuilderClassName(psiClass, psiAnnotation, psiMethod)) ||
           PsiAnnotationSearchUtil.checkAnnotationHasOneOfFQNs(psiAnnotation, LombokClassNames.BUILDER);
  }

  public boolean validate(@Nonnull PsiClass psiClass, @Nonnull PsiAnnotation psiAnnotation, @Nonnull ProblemSink problemSink) {
    boolean result = validateAnnotationOnRightType(psiClass, psiAnnotation, problemSink);
    if (result) {
      final Project project = psiAnnotation.getProject();
      final String builderClassName = getBuilderClassName(psiClass, psiAnnotation);
      final String buildMethodName = getBuildMethodName(psiAnnotation);
      final String builderMethodName = getBuilderMethodName(psiAnnotation);
      result = validateBuilderIdentifier(builderClassName, project, problemSink) &&
               validateBuilderIdentifier(buildMethodName, project, problemSink) &&
               (builderMethodName.isEmpty() || validateBuilderIdentifier(builderMethodName, project, problemSink)) &&
               validateExistingBuilderClass(builderClassName, psiClass, problemSink);
      if (result) {
        final Collection<BuilderInfo> builderInfos = createBuilderInfos(psiClass, null).collect(Collectors.toList());
        result = validateBuilderDefault(builderInfos, problemSink) &&
                 validateSingular(builderInfos, problemSink) &&
                 validateBuilderConstructor(psiClass, builderInfos, problemSink) &&
                 validateObtainViaAnnotations(builderInfos.stream(), problemSink);
      }
    }
    return result;
  }

  protected boolean validateBuilderConstructor(@Nonnull PsiClass psiClass,
                                               Collection<BuilderInfo> builderInfos,
                                               @Nonnull ProblemSink problemSink) {
    if (PsiAnnotationSearchUtil.isAnnotatedWith(psiClass, LombokClassNames.NO_ARGS_CONSTRUCTOR) &&
        PsiAnnotationSearchUtil.isNotAnnotatedWith(psiClass, LombokClassNames.ALL_ARGS_CONSTRUCTOR)) {

      if (PsiAnnotationSearchUtil.isAnnotatedWith(psiClass, LombokClassNames.REQUIRED_ARGS_CONSTRUCTOR)) {
        Collection<PsiField> requiredFields = getNoArgsConstructorProcessor().getRequiredFields(psiClass);
        List<PsiType> requiredTypes = ContainerUtil.map(requiredFields, PsiField::getType);
        List<PsiType> psiTypes = ContainerUtil.map(builderInfos, BuilderInfo::getFieldType);
        if (requiredTypes.equals(psiTypes)) {
          return true;
        }
      }

      Optional<PsiMethod> existingConstructorForParameters = getExistingConstructorForParameters(psiClass, builderInfos);
      if (existingConstructorForParameters.isPresent()) {
        return true;
      }

      if (builderInfos.isEmpty() &&
          PsiClassUtil.collectClassConstructorIntern(psiClass).isEmpty()) {
        return true;
      }

      problemSink.addErrorMessage("inspection.message.lombok.builder.needs.proper.constructor.for.this.class");
      return false;
    }
    return true;
  }

  private static boolean validateBuilderDefault(@Nonnull Collection<BuilderInfo> builderInfos, @Nonnull ProblemSink problemSink) {
    final Optional<BuilderInfo> anyBuilderDefaultAndSingulars = builderInfos.stream()
      .filter(BuilderInfo::hasBuilderDefaultAnnotation)
      .filter(BuilderInfo::hasSingularAnnotation).findAny();
    anyBuilderDefaultAndSingulars.ifPresent(builderInfo -> {
                                              problemSink.addErrorMessage("inspection.message.builder.default.singular.cannot.be.mixed");
                                            }
    );

    final Optional<BuilderInfo> anyBuilderDefaultWithoutInitializer = builderInfos.stream()
      .filter(BuilderInfo::hasBuilderDefaultAnnotation)
      .filter(BuilderInfo::hasNoInitializer).findAny();
    anyBuilderDefaultWithoutInitializer.ifPresent(builderInfo -> {
                                                    problemSink.addErrorMessage("inspection.message.builder.default.requires.initializing.expression");
                                                  }
    );

    return anyBuilderDefaultAndSingulars.isEmpty() || anyBuilderDefaultWithoutInitializer.isEmpty();
  }

  public boolean validate(@Nonnull PsiMethod psiMethod, @Nonnull PsiAnnotation psiAnnotation, @Nonnull ProblemSink problemSink) {
    final PsiClass psiClass = psiMethod.getContainingClass();
    boolean result = null != psiClass;
    if (result) {
      final String builderClassName = getBuilderClassName(psiClass, psiAnnotation, psiMethod);

      final Project project = psiAnnotation.getProject();
      final String buildMethodName = getBuildMethodName(psiAnnotation);
      final String builderMethodName = getBuilderMethodName(psiAnnotation);
      result = validateBuilderIdentifier(builderClassName, project, problemSink) &&
               validateBuilderIdentifier(buildMethodName, project, problemSink) &&
               (builderMethodName.isEmpty() || validateBuilderIdentifier(builderMethodName, project, problemSink)) &&
               validateExistingBuilderClass(builderClassName, psiClass, problemSink);
      if (result) {
        final Stream<BuilderInfo> builderInfos = createBuilderInfos(psiClass, psiMethod);
        result = validateObtainViaAnnotations(builderInfos, problemSink);
      }
    }
    return result;
  }

  private static boolean validateSingular(Collection<BuilderInfo> builderInfos, @Nonnull ProblemSink problemSink) {
    builderInfos.stream().filter(BuilderInfo::hasSingularAnnotation).forEach(builderInfo -> {
      final PsiType psiVariableType = builderInfo.getVariable().getType();

      String qualifiedName = null;
      if (psiVariableType instanceof PsiClassReferenceType psiVariableClassReferenceType) { // can also be PsiArrayType
        qualifiedName = psiVariableClassReferenceType.getClassName();//PsiTypeUtil.getQualifiedName(psiVariableType);
      }

      if (SingularHandlerFactory.isInvalidSingularType(qualifiedName)) {
        problemSink.addErrorMessage("inspection.message.lombok.does.not.know",
                                    qualifiedName != null ? qualifiedName : psiVariableType.getCanonicalText());
        problemSink.markFailed();
      }

      if (!AbstractSingularHandler.validateSingularName(builderInfo.getSingularAnnotation(), builderInfo.getFieldName())) {
        problemSink.addErrorMessage("inspection.message.can.t.singularize.this.name", builderInfo.getFieldName());
        problemSink.markFailed();
      }
    });
    return problemSink.success();
  }

  private static boolean validateBuilderIdentifier(@Nonnull String builderClassName,
                                                   @Nonnull Project project,
                                                   @Nonnull ProblemSink builder) {
    final PsiNameHelper psiNameHelper = PsiNameHelper.getInstance(project);
    if (!psiNameHelper.isIdentifier(builderClassName)) {
      builder.addErrorMessage("inspection.message.s.not.valid.identifier", builderClassName);
      return false;
    }
    return true;
  }

  public boolean validateExistingBuilderClass(@Nonnull String builderClassName,
                                              @Nonnull PsiClass psiClass,
                                              @Nonnull ProblemSink problemSink) {
    final Optional<PsiClass> optionalPsiClass = PsiClassUtil.getInnerClassInternByName(psiClass, builderClassName);

    return optionalPsiClass.map(builderClass -> validateInvalidAnnotationsOnBuilderClass(builderClass, problemSink)).orElse(true);
  }

  boolean validateInvalidAnnotationsOnBuilderClass(@Nonnull PsiClass builderClass, @Nonnull ProblemSink problemSink) {
    if (PsiAnnotationSearchUtil.checkAnnotationsSimpleNameExistsIn(builderClass, INVALID_ON_BUILDERS)) {
      problemSink.addErrorMessage("inspection.message.lombok.annotations.are.not.allowed.on.builder.class");
      return false;
    }
    return true;
  }

  private static boolean validateAnnotationOnRightType(@Nonnull PsiClass psiClass,
                                                       @Nonnull PsiAnnotation psiAnnotation,
                                                       @Nonnull ProblemSink builder) {
    if (psiClass.isAnnotationType() || psiClass.isInterface() || psiClass.isEnum()) {
      builder.addErrorMessage("inspection.message.s.can.be.used.on.classes.only", psiAnnotation.getQualifiedName());
      return false;
    }
    return true;
  }

  private static boolean validateObtainViaAnnotations(Stream<BuilderInfo> builderInfos, @Nonnull ProblemSink problemSink) {
    builderInfos.map(BuilderInfo::withObtainVia)
      .filter(BuilderInfo::hasObtainViaAnnotation)
      .forEach(builderInfo ->
               {
                 if (StringUtil.isEmpty(builderInfo.getViaFieldName()) == StringUtil.isEmpty(builderInfo.getViaMethodName())) {
                   problemSink.addErrorMessage("inspection.message.syntax.either.obtain.via.field");
                   problemSink.markFailed();
                 }

                 if (StringUtil.isEmpty(builderInfo.getViaMethodName()) && builderInfo.isViaStaticCall()) {
                   problemSink.addErrorMessage("inspection.message.obtain.via.is.static.true.not.valid.unless.method.has.been.set");
                   problemSink.markFailed();
                 }
               });
    return problemSink.success();
  }

  public Optional<PsiClass> getExistInnerBuilderClass(@Nonnull PsiClass psiClass,
                                                      @Nullable PsiMethod psiMethod,
                                                      @Nonnull PsiAnnotation psiAnnotation) {
    final String builderClassName = getBuilderClassName(psiClass, psiAnnotation, psiMethod);
    return PsiClassUtil.getInnerClassInternByName(psiClass, builderClassName);
  }

  PsiType getReturnTypeOfBuildMethod(@Nonnull PsiClass psiClass, @Nullable PsiMethod psiMethod) {
    final PsiType result;
    if (null == psiMethod || psiMethod.isConstructor()) {
      result = PsiClassUtil.getTypeWithGenerics(psiClass);
    }
    else {
      result = psiMethod.getReturnType();
    }
    return result;
  }

  @Nonnull
  public static String getBuildMethodName(@Nonnull PsiAnnotation psiAnnotation) {
    final String buildMethodName =
      PsiAnnotationUtil.getStringAnnotationValue(psiAnnotation, ANNOTATION_BUILD_METHOD_NAME, BUILD_METHOD_NAME);
    return StringUtil.isEmptyOrSpaces(buildMethodName) ? BUILD_METHOD_NAME : buildMethodName;
  }

  @Nonnull
  public String getBuilderMethodName(@Nonnull PsiAnnotation psiAnnotation) {
    final String builderMethodName =
      PsiAnnotationUtil.getStringAnnotationValue(psiAnnotation, ANNOTATION_BUILDER_METHOD_NAME, BUILDER_METHOD_NAME);
    return null == builderMethodName ? BUILDER_METHOD_NAME : builderMethodName;
  }

  @Nonnull
  private static String getSetterPrefix(@Nonnull PsiAnnotation psiAnnotation) {
    final String setterPrefix = PsiAnnotationUtil.getStringAnnotationValue(psiAnnotation, ANNOTATION_SETTER_PREFIX, "");
    return null == setterPrefix ? "" : setterPrefix;
  }

  @Nonnull
  @PsiModifier.ModifierConstant
  private static String getBuilderOuterAccessVisibility(@Nonnull PsiAnnotation psiAnnotation) {
    final String accessVisibility = LombokProcessorUtil.getAccessVisibility(psiAnnotation);
    return null == accessVisibility ? PsiModifier.PUBLIC : accessVisibility;
  }

  @Nonnull
  @PsiModifier.ModifierConstant
  private static String getBuilderInnerAccessVisibility(@Nonnull PsiAnnotation psiAnnotation) {
    final String accessVisibility = getBuilderOuterAccessVisibility(psiAnnotation);
    return PsiModifier.PROTECTED.equals(accessVisibility) ? PsiModifier.PUBLIC : accessVisibility;
  }

  @Nonnull
  private static String getBuilderClassName(@Nonnull PsiClass psiClass, @Nonnull PsiAnnotation psiAnnotation) {
    return getBuilderClassName(psiClass, psiAnnotation, null);
  }

  @Nonnull
  public static String getBuilderClassName(@Nonnull PsiClass psiClass,
                                           @Nonnull PsiAnnotation psiAnnotation,
                                           @Nullable PsiMethod psiMethod) {
    final String builderClassName = PsiAnnotationUtil.getStringAnnotationValue(psiAnnotation, ANNOTATION_BUILDER_CLASS_NAME, "");
    if (!StringUtil.isEmptyOrSpaces(builderClassName)) {
      return builderClassName;
    }

    String relevantReturnType = psiClass.getName();

    if (null != psiMethod && !psiMethod.isConstructor()) {
      final PsiType psiMethodReturnType = psiMethod.getReturnType();
      if (null != psiMethodReturnType) {
        relevantReturnType = PsiNameHelper.getQualifiedClassName(psiMethodReturnType.getPresentableText(), false);
      }
    }

    return getBuilderClassName(psiClass, relevantReturnType);
  }

  @Nonnull
  static String getBuilderClassName(@Nonnull PsiClass psiClass, String returnTypeName) {
    final ConfigDiscovery configDiscovery = ConfigDiscovery.getInstance();
    final String builderClassNamePattern = configDiscovery.getStringLombokConfigProperty(BUILDER_CLASS_NAME, psiClass);
    return replace(builderClassNamePattern, "*", capitalize(returnTypeName));
  }

  boolean hasMethod(@Nonnull PsiClass psiClass, @Nonnull String builderMethodName) {
    final Collection<PsiMethod> existingMethods = PsiClassUtil.collectClassStaticMethodsIntern(psiClass);
    return existingMethods.stream().map(PsiMethod::getName).anyMatch(builderMethodName::equals);
  }

  public Collection<PsiMethod> createBuilderDefaultProviderMethodsIfNecessary(@Nonnull PsiClass containingClass,
                                                                              @Nullable PsiMethod psiMethod,
                                                                              @Nonnull PsiClass builderPsiClass,
                                                                              @Nonnull PsiAnnotation psiAnnotation) {
    final List<BuilderInfo> builderInfos = createBuilderInfos(psiAnnotation, containingClass, psiMethod, builderPsiClass);
    return builderInfos.stream()
      .filter(BuilderInfo::hasBuilderDefaultAnnotation)
      .filter(b -> !b.hasSingularAnnotation())
      .filter(b -> !b.hasNoInitializer())
      .map(BuilderHandler::createBuilderDefaultProviderMethod)
      .collect(Collectors.toList());
  }

  private static PsiMethod createBuilderDefaultProviderMethod(@Nonnull BuilderInfo info) {
    final PsiClass builderClass = info.getBuilderClass();
    final PsiClass containingClass = builderClass.getContainingClass();
    final String blockText = String.format("return %s;", info.getFieldInitializer().getText());

    return new LombokLightMethodBuilder(builderClass.getManager(), info.renderFieldDefaultProviderName())
      .withMethodReturnType(info.getFieldType())
      .withContainingClass(containingClass)
      .withNavigationElement(info.getVariable())
      .withModifier(PsiModifier.PRIVATE)
      .withModifier(PsiModifier.STATIC)
      .withBodyText(blockText);
  }

  public Optional<PsiMethod> createBuilderMethodIfNecessary(@Nonnull PsiClass containingClass,
                                                            @Nullable PsiMethod psiMethod,
                                                            @Nonnull PsiClass builderPsiClass,
                                                            @Nonnull PsiAnnotation psiAnnotation) {
    final String builderMethodName = getBuilderMethodName(psiAnnotation);
    if (!builderMethodName.isEmpty() && !hasMethod(containingClass, builderMethodName)) {
      final PsiType psiTypeWithGenerics = PsiClassUtil.getTypeWithGenerics(builderPsiClass);

      final String blockText = String.format("return new %s();", psiTypeWithGenerics.getCanonicalText(false));
      final LombokLightMethodBuilder methodBuilder = new LombokLightMethodBuilder(containingClass.getManager(), builderMethodName)
        .withMethodReturnType(psiTypeWithGenerics)
        .withContainingClass(containingClass)
        .withNavigationElement(psiAnnotation)
        .withModifier(getBuilderOuterAccessVisibility(psiAnnotation))
        .withBodyText(blockText);

      addTypeParameters(builderPsiClass, psiMethod, methodBuilder);

      if (null == psiMethod || psiMethod.isConstructor() || psiMethod.hasModifierProperty(PsiModifier.STATIC)) {
        methodBuilder.withModifier(PsiModifier.STATIC);
      }

      LombokAddNullAnnotations.createRelevantNonNullAnnotation(containingClass, methodBuilder);

      return Optional.of(methodBuilder);
    }
    return Optional.empty();
  }

  public Optional<PsiMethod> createToBuilderMethodIfNecessary(@Nonnull PsiClass containingClass,
                                                              @Nullable PsiMethod psiMethod,
                                                              @Nonnull PsiClass builderPsiClass,
                                                              @Nonnull PsiAnnotation psiAnnotation) {
    if (!PsiAnnotationUtil.getBooleanAnnotationValue(psiAnnotation, TO_BUILDER_ANNOTATION_KEY, false)) {
      return Optional.empty();
    }

    final List<BuilderInfo> builderInfos = createBuilderInfos(psiAnnotation, containingClass, psiMethod, builderPsiClass);
    builderInfos.forEach(BuilderInfo::withObtainVia);

    final PsiType psiTypeWithGenerics;
    if (null != psiMethod) {
      psiTypeWithGenerics = calculateResultType(builderInfos, builderPsiClass, containingClass);
    }
    else {
      psiTypeWithGenerics = PsiClassUtil.getTypeWithGenerics(builderPsiClass);
    }

    final LombokLightMethodBuilder methodBuilder = new LombokLightMethodBuilder(containingClass.getManager(), TO_BUILDER_METHOD_NAME)
      .withMethodReturnType(psiTypeWithGenerics)
      .withContainingClass(containingClass)
      .withNavigationElement(psiAnnotation)
      .withModifier(getBuilderOuterAccessVisibility(psiAnnotation));

    final String toBuilderPrependStatements = builderInfos.stream()
      .map(BuilderInfo::renderToBuilderPrependStatement)
      .filter(Predicate.not(StringUtil::isEmpty))
      .collect(Collectors.joining("\n"));

    final String toBuilderMethodCalls = builderInfos.stream()
      .map(BuilderInfo::renderToBuilderCallWithPrependLogic)
      .filter(Predicate.not(StringUtil::isEmpty))
      .collect(Collectors.joining(".", ".", ""));

    final String toBuilderAppendStatements = builderInfos.stream()
      .map(BuilderInfo::renderToBuilderAppendStatement)
      .filter(Predicate.not(StringUtil::isEmpty))
      .collect(Collectors.joining("\n"));

    final String canonicalText = psiTypeWithGenerics.getCanonicalText(false);
    final String blockText;
    if (toBuilderAppendStatements.isEmpty()) {
      blockText = toBuilderPrependStatements +
                  String.format("\nreturn new %s()%s;", canonicalText, toBuilderMethodCalls);
    }
    else {
      blockText = toBuilderPrependStatements +
                  String.format("\nfinal %s %s = new %s()%s;\n", canonicalText, BUILDER_TEMP_VAR, canonicalText, toBuilderMethodCalls) +
                  toBuilderAppendStatements +
                  String.format("\nreturn %s;", BUILDER_TEMP_VAR);
    }

    methodBuilder.withBodyText(blockText);

    LombokAddNullAnnotations.createRelevantNonNullAnnotation(containingClass, methodBuilder);

    return Optional.of(methodBuilder);
  }

  private static PsiType calculateResultType(@Nonnull List<BuilderInfo> builderInfos, PsiClass builderPsiClass, PsiClass psiClass) {
    final PsiElementFactory factory = JavaPsiFacade.getElementFactory(psiClass.getProject());
    final PsiType[] psiTypes = builderInfos.stream()
      .map(BuilderInfo::getObtainViaFieldVariableType)
      .filter(Optional::isPresent)
      .map(Optional::get)
      .toArray(PsiType[]::new);
    return factory.createType(builderPsiClass, psiTypes);
  }

  @Nonnull
  private static Stream<BuilderInfo> createBuilderInfos(@Nonnull PsiClass psiClass, @Nullable PsiMethod psiClassMethod) {
    final Stream<BuilderInfo> result;
    if (null != psiClassMethod) {
      result = Arrays.stream(psiClassMethod.getParameterList().getParameters()).map(BuilderInfo::fromPsiParameter);
    }
    else if (psiClass.isRecord()) {
      result = Arrays.stream(psiClass.getRecordComponents()).map(BuilderInfo::fromPsiRecordComponent);
    }
    else {
      result = PsiClassUtil.collectClassFieldsIntern(psiClass).stream().map(BuilderInfo::fromPsiField)
        .filter(BuilderInfo::useForBuilder);
    }
    return result;
  }

  public List<BuilderInfo> createBuilderInfos(@Nonnull PsiAnnotation psiAnnotation, @Nonnull PsiClass psiClass,
                                              @Nullable PsiMethod psiClassMethod, @Nonnull PsiClass builderClass) {
    final PsiSubstitutor builderSubstitutor = getBuilderSubstitutor(psiClass, builderClass);
    final String accessVisibility = getBuilderInnerAccessVisibility(psiAnnotation);
    final String setterPrefix = getSetterPrefix(psiAnnotation);
    final LombokNullAnnotationLibrary nullAnnotationLibrary = ConfigDiscovery.getInstance().getAddNullAnnotationLombokConfigProperty(psiClass);

    return createBuilderInfos(psiClass, psiClassMethod)
      .map(info -> info.withSubstitutor(builderSubstitutor))
      .map(info -> info.withBuilderClass(builderClass))
      .map(info -> info.withVisibilityModifier(accessVisibility))
      .map(info -> info.withSetterPrefix(setterPrefix))
      .map(info -> info.withNullAnnotationLibrary(nullAnnotationLibrary))
      .collect(Collectors.toList());
  }

  @Nonnull
  public PsiClass createBuilderClass(@Nonnull PsiClass psiClass, @Nullable PsiMethod psiMethod, @Nonnull PsiAnnotation psiAnnotation) {
    final LombokLightClassBuilder builderClass;
    if (null != psiMethod) {
      builderClass = createEmptyBuilderClass(psiClass, psiMethod, psiAnnotation);
    }
    else {
      builderClass = createEmptyBuilderClass(psiClass, psiAnnotation);
    }

    if (hasValidJacksonizedAnnotation(psiClass, psiMethod)) {
      handleJacksonized(psiClass, psiMethod, psiAnnotation, builderClass);
    }

    builderClass.withFieldSupplier((thisPsiClass) -> {
      final List<BuilderInfo> builderInfos = createBuilderInfos(psiAnnotation, psiClass, psiMethod, thisPsiClass);
      // create builder Fields
      return builderInfos.stream()
        .map(BuilderInfo::renderBuilderFields)
        .flatMap(Collection::stream)
        .collect(Collectors.toList());
    });

    builderClass.withMethodSupplier((thisPsiClass) -> {
      Collection<PsiMethod> psiMethods = new ArrayList<>(createConstructors(thisPsiClass, psiAnnotation));

      final List<BuilderInfo> builderInfos = createBuilderInfos(psiAnnotation, psiClass, psiMethod, thisPsiClass);
      // create builder methods
      builderInfos.stream()
        .map(BuilderInfo::renderBuilderMethods)
        .forEach(psiMethods::addAll);

      // create 'build' method
      final String buildMethodName = getBuildMethodName(psiAnnotation);
      psiMethods.add(createBuildMethod(psiAnnotation, psiClass, psiMethod, thisPsiClass, buildMethodName, builderInfos));

      // create 'toString' method
      psiMethods.add(createToStringMethod(psiAnnotation, thisPsiClass));

      return psiMethods;
    });

    return builderClass;
  }

  static boolean hasValidJacksonizedAnnotation(@Nonnull PsiClass psiClass, @Nullable PsiMethod psiMethod) {
    final boolean hasJacksonizedAnnotation =
      PsiAnnotationSearchUtil.isAnnotatedWith(null == psiMethod ? psiClass : psiMethod, LombokClassNames.JACKSONIZED);
    return hasJacksonizedAnnotation &&
           JacksonizedProcessor.validateAnnotationOwner(null == psiMethod ? psiClass : psiMethod, new ProblemProcessingSink());
  }

  static void handleJacksonized(@Nonnull PsiClass psiClass,
                                @Nullable PsiMethod psiMethod,
                                @Nonnull PsiAnnotation psiAnnotation,
                                @Nonnull LombokLightClassBuilder builderClass) {
    //Annotation 'com.fasterxml.jackson.databind.annotation.JsonDeserialize(builder=Foobar.FoobarBuilder[Impl].class)' should be added on PsiClass
    psiClass.putUserData(LombokUserDataKeys.AUGMENTED_ANNOTATIONS,
                         Collections.singleton(
                           "@com.fasterxml.jackson.databind.annotation.JsonDeserialize(builder=" +
                           builderClass.getQualifiedName() + ".class)"));

    //add com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder(withPrefix="with", buildMethodName="build")
    final PsiAnnotation jsonPojoBuilderAnnotation = createPojoBuilderAnnotation(psiClass, psiAnnotation);
    builderClass.getModifierList().withAnnotation(jsonPojoBuilderAnnotation);

    LombokCopyableAnnotations.copyCopyableAnnotations(null == psiMethod ? psiClass : psiMethod,
                                                      builderClass.getModifierList(),
                                                      LombokCopyableAnnotations.JACKSON_COPY_TO_BUILDER);
  }

  @Nonnull
  private static PsiAnnotation createPojoBuilderAnnotation(@Nonnull PsiClass psiClass, @Nonnull PsiAnnotation psiAnnotation) {
    final StringBuilder parameters = new StringBuilder();
    final String setterPrefix = getSetterPrefix(psiAnnotation);
    parameters.append("withPrefix=\"");
    parameters.append(setterPrefix);
    parameters.append('"');
    parameters.append(',');

    final String buildMethodName = getBuildMethodName(psiAnnotation);
    parameters.append("buildMethodName=\"");
    parameters.append(buildMethodName);
    parameters.append('"');

    return JavaPsiFacade.getElementFactory(psiClass.getProject())
      .createAnnotationFromText('@' + JACKSON_DATABIND_ANNOTATION_JSON_POJOBUILDER + "(" + parameters + ")", psiClass);
  }

  @Nonnull
  private static LombokLightClassBuilder createEmptyBuilderClass(@Nonnull PsiClass psiClass,
                                                                 @Nonnull PsiMethod psiMethod,
                                                                 @Nonnull PsiAnnotation psiAnnotation) {
    return createBuilderClass(psiClass, psiMethod,
                              psiMethod.isConstructor() || psiMethod.hasModifierProperty(PsiModifier.STATIC), psiAnnotation);
  }

  @Nonnull
  private static LombokLightClassBuilder createEmptyBuilderClass(@Nonnull PsiClass psiClass, @Nonnull PsiAnnotation psiAnnotation) {
    return createBuilderClass(psiClass, psiClass, true, psiAnnotation);
  }

  public Optional<PsiClass> createBuilderClassIfNotExist(@Nonnull PsiClass psiClass,
                                                         @Nullable PsiMethod psiMethod,
                                                         @Nonnull PsiAnnotation psiAnnotation) {
    PsiClass builderClass = null;
    if (getExistInnerBuilderClass(psiClass, psiMethod, psiAnnotation).isEmpty()) {
      builderClass = createBuilderClass(psiClass, psiMethod, psiAnnotation);
    }
    return Optional.ofNullable(builderClass);
  }

  @Nonnull
  public PsiMethod createToStringMethod(@Nonnull PsiAnnotation psiAnnotation, @Nonnull PsiClass builderClass) {
    return createToStringMethod(psiAnnotation, builderClass, false);
  }

  @Nonnull
  PsiMethod createToStringMethod(@Nonnull PsiAnnotation psiAnnotation, @Nonnull PsiClass builderClass, boolean forceCallSuper) {
    final List<EqualsAndHashCodeToStringHandler.MemberInfo> memberInfos = Arrays.stream(builderClass.getFields())
      .filter(BuilderHandler::isNotBuilderDefaultSetterFields)
      .map(EqualsAndHashCodeToStringHandler.MemberInfo::new).collect(Collectors.toList());
    return getToStringProcessor().createToStringMethod(builderClass, memberInfos, psiAnnotation, forceCallSuper);
  }

  private static boolean isNotBuilderDefaultSetterFields(@Nonnull PsiField psiField) {
    boolean isBuilderDefaultSetter = false;
    if (psiField.getName().endsWith("$set") && PsiTypes.booleanType().equals(psiField.getType())) {
      PsiElement navigationElement = psiField.getNavigationElement();
      if (navigationElement instanceof PsiField) {
        isBuilderDefaultSetter = PsiAnnotationSearchUtil.isAnnotatedWith((PsiField)navigationElement, LombokClassNames.BUILDER_DEFAULT);
      }
    }
    return !isBuilderDefaultSetter;
  }

  @Nonnull
  private static LombokLightClassBuilder createBuilderClass(@Nonnull PsiClass psiClass,
                                                            @Nonnull PsiTypeParameterListOwner psiTypeParameterListOwner,
                                                            final boolean isStatic,
                                                            @Nonnull PsiAnnotation psiAnnotation) {
    PsiMethod psiMethod = null;
    if (psiTypeParameterListOwner instanceof PsiMethod) {
      psiMethod = (PsiMethod)psiTypeParameterListOwner;
    }

    final String builderClassName = getBuilderClassName(psiClass, psiAnnotation, psiMethod);
    final String builderClassQualifiedName = psiClass.getQualifiedName() + "." + builderClassName;

    final LombokLightClassBuilder classBuilder = new LombokLightClassBuilder(psiClass, builderClassName, builderClassQualifiedName)
      .withContainingClass(psiClass)
      .withNavigationElement(psiAnnotation)
      .withParameterTypes((null != psiMethod && psiMethod.isConstructor())
                          ? psiClass.getTypeParameterList()
                          : psiTypeParameterListOwner.getTypeParameterList())
      .withModifier(getBuilderOuterAccessVisibility(psiAnnotation));
    if (isStatic) {
      classBuilder.withModifier(PsiModifier.STATIC);
    }
    return classBuilder;
  }

  @Nonnull
  public static Collection<PsiMethod> createConstructors(@Nonnull PsiClass psiClass, @Nonnull PsiAnnotation psiAnnotation) {
    final Collection<PsiMethod> methodsIntern = PsiClassUtil.collectClassConstructorIntern(psiClass);
    final NoArgsConstructorProcessor noArgsConstructorProcessor = getNoArgsConstructorProcessor();
    final String constructorName = noArgsConstructorProcessor.getConstructorName(psiClass);
    for (PsiMethod existedConstructor : methodsIntern) {
      if (constructorName.equals(existedConstructor.getName()) && existedConstructor.getParameterList().getParametersCount() == 0) {
        return Collections.emptySet();
      }
    }
    return noArgsConstructorProcessor.createNoArgsConstructor(psiClass, PsiModifier.PACKAGE_LOCAL, psiAnnotation);
  }

  @Nonnull
  public PsiMethod createBuildMethod(@Nonnull PsiAnnotation psiAnnotation,
                                     @Nonnull PsiClass parentClass,
                                     @Nullable PsiMethod psiMethod,
                                     @Nonnull PsiClass builderClass,
                                     @Nonnull String buildMethodName,
                                     List<BuilderInfo> builderInfos) {
    final PsiType builderType = getReturnTypeOfBuildMethod(parentClass, psiMethod);

    final PsiSubstitutor builderSubstitutor = getBuilderSubstitutor(parentClass, builderClass);
    final PsiType returnType = builderSubstitutor.substitute(builderType);

    final String buildMethodPrepare = builderInfos.stream()
      .map(BuilderInfo::renderBuildPrepare)
      .collect(Collectors.joining());

    final String buildMethodParameters = builderInfos.stream()
      .map(BuilderInfo::renderBuildCall)
      .collect(Collectors.joining(","));

    final LombokLightMethodBuilder methodBuilder = new LombokLightMethodBuilder(parentClass.getManager(), buildMethodName)
      .withMethodReturnType(returnType)
      .withContainingClass(builderClass)
      .withNavigationElement(parentClass)
      .withModifier(getBuilderInnerAccessVisibility(psiAnnotation));
    final String codeBlockText =
      createBuildMethodCodeBlockText(psiMethod, builderClass, returnType, buildMethodPrepare, buildMethodParameters);
    methodBuilder.withBodyText(codeBlockText);

    if (!PsiTypes.voidType().equals(returnType)) {
      LombokAddNullAnnotations.createRelevantNonNullAnnotation(builderClass, methodBuilder);
    }

    Optional<PsiMethod> definedConstructor = Optional.ofNullable(psiMethod);
    if (definedConstructor.isEmpty()) {
      definedConstructor = getExistingConstructorForParameters(parentClass, builderInfos);
    }
    definedConstructor.map(PsiMethod::getThrowsList).map(PsiReferenceList::getReferencedTypes).map(Arrays::stream)
      .ifPresent(stream -> stream.forEach(methodBuilder::withException));

    return methodBuilder;
  }

  private static Optional<PsiMethod> getExistingConstructorForParameters(@Nonnull PsiClass parentClass,
                                                                         Collection<BuilderInfo> builderInfos) {
    final Collection<PsiMethod> classConstructors = PsiClassUtil.collectClassConstructorIntern(parentClass);
    return classConstructors.stream()
      .filter(m -> sameParameters(m.getParameterList().getParameters(), builderInfos))
      .findFirst();
  }

  private static boolean sameParameters(PsiParameter[] parameters, Collection<BuilderInfo> builderInfos) {
    if (parameters.length != builderInfos.size()) {
      return false;
    }

    final Iterator<BuilderInfo> builderInfoIterator = builderInfos.iterator();
    for (PsiParameter psiParameter : parameters) {
      final BuilderInfo builderInfo = builderInfoIterator.next();
      if (!psiParameter.getType().isAssignableFrom(builderInfo.getFieldType())) {
        return false;
      }
    }
    return true;
  }

  @Nonnull
  private static String createBuildMethodCodeBlockText(@Nullable PsiMethod psiMethod,
                                                       @Nonnull PsiClass psiClass,
                                                       @Nonnull PsiType buildMethodReturnType,
                                                       @Nonnull String buildMethodPrepare,
                                                       @Nonnull String buildMethodParameters) {
    final String blockText;

    final String codeBlockFormat, callExpressionText;

    if (null == psiMethod || psiMethod.isConstructor()) {
      codeBlockFormat = "%s\n return new %s(%s);";
      callExpressionText = buildMethodReturnType.getPresentableText();
    }
    else {
      if (PsiTypes.voidType().equals(buildMethodReturnType)) {
        codeBlockFormat = "%s\n %s(%s);";
      }
      else {
        codeBlockFormat = "%s\n return %s(%s);";
      }
      callExpressionText = calculateCallExpressionForMethod(psiMethod, psiClass);
    }
    blockText = String.format(codeBlockFormat, buildMethodPrepare, callExpressionText, buildMethodParameters);
    return blockText;
  }

  @Nonnull
  private static String calculateCallExpressionForMethod(@Nonnull PsiMethod psiMethod, @Nonnull PsiClass builderClass) {
    final PsiClass containingClass = psiMethod.getContainingClass();

    StringBuilder className = new StringBuilder();
    if (null != containingClass) {
      className.append(containingClass.getName()).append(".");
      if (!psiMethod.isConstructor() && !psiMethod.hasModifierProperty(PsiModifier.STATIC)) {
        className.append("this.");
      }
      if (builderClass.hasTypeParameters()) {
        className.append(
          Arrays.stream(builderClass.getTypeParameters()).map(PsiTypeParameter::getName).collect(Collectors.joining(",", "<", ">")));
      }
    }
    return className + psiMethod.getName();
  }

  void addTypeParameters(@Nonnull PsiClass builderClass, @Nullable PsiMethod psiMethod, @Nonnull LombokLightMethodBuilder methodBuilder) {
    final PsiTypeParameter[] psiTypeParameters;
    if (null == psiMethod || psiMethod.isConstructor()) {
      psiTypeParameters = builderClass.getTypeParameters();
    }
    else {
      psiTypeParameters = psiMethod.getTypeParameters();
    }

    for (PsiTypeParameter psiTypeParameter : psiTypeParameters) {
      methodBuilder.withTypeParameter(psiTypeParameter);
    }
  }

  private static NoArgsConstructorProcessor getNoArgsConstructorProcessor() {
    return ProcessorUtil.getProcessor(NoArgsConstructorProcessor.class);
  }

  private static ToStringProcessor getToStringProcessor() {
    return ProcessorUtil.getProcessor(ToStringProcessor.class);
  }
}
