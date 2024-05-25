package de.plushnikov.intellij.plugin.processor.clazz.constructor;

import com.intellij.java.impl.codeInsight.daemon.impl.quickfix.SafeDeleteFix;
import com.intellij.java.language.impl.psi.impl.RecordAugmentProvider;
import com.intellij.java.language.impl.psi.impl.light.LightReferenceListBuilder;
import com.intellij.java.language.impl.psi.impl.light.LightTypeParameterBuilder;
import com.intellij.java.language.psi.*;
import com.intellij.java.language.psi.util.PsiTypesUtil;
import consulo.language.psi.PsiElement;
import consulo.util.collection.ContainerUtil;
import consulo.util.lang.StringUtil;
import de.plushnikov.intellij.plugin.LombokClassNames;
import de.plushnikov.intellij.plugin.lombokconfig.ConfigKey;
import de.plushnikov.intellij.plugin.problem.ProblemProcessingSink;
import de.plushnikov.intellij.plugin.problem.ProblemSink;
import de.plushnikov.intellij.plugin.processor.clazz.AbstractClassProcessor;
import de.plushnikov.intellij.plugin.processor.field.AccessorsInfo;
import de.plushnikov.intellij.plugin.psi.LombokLightMethodBuilder;
import de.plushnikov.intellij.plugin.psi.LombokLightParameter;
import de.plushnikov.intellij.plugin.thirdparty.LombokAddNullAnnotations;
import de.plushnikov.intellij.plugin.thirdparty.LombokCopyableAnnotations;
import de.plushnikov.intellij.plugin.thirdparty.LombokUtils;
import de.plushnikov.intellij.plugin.util.*;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Base lombok processor class for constructor processing
 *
 * @author Plushnikov Michail
 */
public abstract class AbstractConstructorClassProcessor extends AbstractClassProcessor {
  private static final String BUILDER_DEFAULT_ANNOTATION = LombokClassNames.BUILDER_DEFAULT;

  AbstractConstructorClassProcessor(@Nonnull String supportedAnnotationClass,
                                    @Nonnull Class<? extends PsiElement> supportedClass) {
    super(supportedClass, supportedAnnotationClass);
  }

  @Override
  public Collection<String> getNamesOfPossibleGeneratedElements(@Nonnull PsiClass psiClass, @Nonnull PsiAnnotation psiAnnotation) {
    return List.of(getConstructorName(psiClass), getStaticConstructorName(psiAnnotation));
  }

  @Override
  protected boolean validate(@Nonnull PsiAnnotation psiAnnotation, @Nonnull PsiClass psiClass, @Nonnull ProblemSink builder) {
    boolean result = true;
    if (!validateAnnotationOnRightType(psiClass, builder)) {
      result = false;
    }
    if (!validateVisibility(psiAnnotation)) {
      result = false;
    }

    if (!validateBaseClassConstructor(psiClass, builder)) {
      result = false;
    }
    return result;
  }

  private static boolean validateVisibility(@Nonnull PsiAnnotation psiAnnotation) {
    final String visibility = LombokProcessorUtil.getAccessVisibility(psiAnnotation);
    return null != visibility;
  }

  private static boolean validateAnnotationOnRightType(@Nonnull PsiClass psiClass, @Nonnull ProblemSink builder) {
    boolean result = true;
    if (psiClass.isAnnotationType() || psiClass.isInterface()) {
      builder.addErrorMessage("inspection.message.annotation.only.supported.on.class.or.enum.type");
      result = false;
    }
    return result;
  }

  public boolean validateBaseClassConstructor(@Nonnull PsiClass psiClass, @Nonnull ProblemSink builder) {
    if (psiClass instanceof PsiAnonymousClass || psiClass.isEnum()) {
      return true;
    }
    PsiClass baseClass = psiClass.getSuperClass();
    if (baseClass == null || psiClass.getManager().areElementsEquivalent(psiClass, baseClass)) {
      return true;
    }
    PsiMethod[] constructors = baseClass.getConstructors();
    if (constructors.length == 0) {
      return true;
    }

    for (PsiMethod constructor : constructors) {
      final int parametersCount = constructor.getParameterList().getParametersCount();
      if (parametersCount == 0 || parametersCount == 1 && constructor.isVarArgs()) {
        return true;
      }
    }
    builder.addErrorMessage("inspection.message.lombok.needs.default.constructor.in.base.class");
    return false;
  }

  private static boolean validateIsStaticConstructorNotDefined(@Nonnull PsiClass psiClass,
                                                               @Nullable String staticConstructorName,
                                                               @Nonnull Collection<PsiField> params,
                                                               @Nonnull ProblemSink builder) {
    boolean result = true;

    final List<PsiType> paramTypes = new ArrayList<>(params.size());
    for (PsiField param : params) {
      paramTypes.add(param.getType());
    }

    if (isStaticConstructor(staticConstructorName)) {
      final Collection<PsiMethod> definedMethods = PsiClassUtil.collectClassStaticMethodsIntern(psiClass);

      final PsiMethod existedStaticMethod = findExistedMethod(definedMethods, staticConstructorName, paramTypes);
      if (null != existedStaticMethod) {
        if (paramTypes.isEmpty()) {
          builder.addErrorMessage("inspection.message.method.s.matched.static.constructor.name.already.defined", staticConstructorName)
            .withLocalQuickFixes(() -> new SafeDeleteFix(existedStaticMethod));
        }
        else {
          builder.addErrorMessage("inspection.message.method.s.with.d.parameters.matched.static.constructor.name.already.defined",
                                  staticConstructorName, paramTypes.size())
            .withLocalQuickFixes(() -> new SafeDeleteFix(existedStaticMethod));
        }
        result = false;
      }
    }

    return result;
  }

  public boolean validateIsConstructorNotDefined(@Nonnull PsiClass psiClass, @Nullable String staticConstructorName,
                                                 @Nonnull Collection<PsiField> params, @Nonnull ProblemSink builder) {
    // Constructor not defined or static constructor not defined
    return validateIsConstructorNotDefined(psiClass, params, builder) ||
           validateIsStaticConstructorNotDefined(psiClass, staticConstructorName, params, builder);
  }

  private boolean validateIsConstructorNotDefined(@Nonnull PsiClass psiClass, @Nonnull Collection<PsiField> params,
                                                  @Nonnull ProblemSink builder) {
    boolean result = true;

    final List<PsiType> paramTypes = ContainerUtil.map(params, PsiField::getType);
    final Collection<PsiMethod> definedConstructors = PsiClassUtil.collectClassConstructorIntern(psiClass);
    final String constructorName = getConstructorName(psiClass);

    final PsiMethod existedMethod = findExistedMethod(definedConstructors, constructorName, paramTypes);
    if (null != existedMethod) {
      if (paramTypes.isEmpty()) {
        builder.addErrorMessage("inspection.message.constructor.without.parameters.already.defined")
          .withLocalQuickFixes(() -> new SafeDeleteFix(existedMethod));
      }
      else {
        builder.addErrorMessage("inspection.message.constructor.with.d.parameters.already.defined", paramTypes.size())
          .withLocalQuickFixes(() -> new SafeDeleteFix(existedMethod));
      }
      result = false;
    }

    return result;
  }

  @Nonnull
  public String getConstructorName(@Nonnull PsiClass psiClass) {
    return StringUtil.notNullize(psiClass.getName());
  }

  @Nullable
  private static PsiMethod findExistedMethod(final Collection<PsiMethod> definedMethods,
                                             final String methodName,
                                             final List<PsiType> paramTypes) {
    for (PsiMethod method : definedMethods) {
      if (PsiElementUtil.methodMatches(method, null, null, methodName, paramTypes)) {
        return method;
      }
    }
    return null;
  }

  @Nonnull
  protected static Collection<PsiField> getAllNotInitializedAndNotStaticFields(@Nonnull PsiClass psiClass) {
    Collection<PsiField> allNotInitializedNotStaticFields = new ArrayList<>();
    final boolean classAnnotatedWithValue = PsiAnnotationSearchUtil.isAnnotatedWith(psiClass, LombokClassNames.VALUE);
    Collection<PsiField> fields = psiClass.isRecord() ? RecordAugmentProvider.getFieldAugments(psiClass)
                                                      : PsiClassUtil.collectClassFieldsIntern(psiClass);
    for (PsiField psiField : fields) {
      // skip fields named $
      boolean addField = !psiField.getName().startsWith(LombokUtils.LOMBOK_INTERN_FIELD_MARKER);

      final PsiModifierList modifierList = psiField.getModifierList();
      if (null != modifierList) {
        // skip static fields
        addField &= !modifierList.hasModifierProperty(PsiModifier.STATIC);

        boolean isFinal = isFieldFinal(psiField, modifierList, classAnnotatedWithValue);
        // skip initialized final fields
        addField &= (!isFinal || !psiField.hasInitializer() ||
                     PsiAnnotationSearchUtil.findAnnotation(psiField, BUILDER_DEFAULT_ANNOTATION) != null);
      }

      if (addField) {
        allNotInitializedNotStaticFields.add(psiField);
      }
    }
    return allNotInitializedNotStaticFields;
  }

  @Nonnull
  public static Collection<PsiField> getAllFields(@Nonnull PsiClass psiClass) {
    return getAllNotInitializedAndNotStaticFields(psiClass);
  }

  @Nonnull
  public Collection<PsiField> getRequiredFields(@Nonnull PsiClass psiClass) {
    return getRequiredFields(psiClass, false);
  }

  @Nonnull
  Collection<PsiField> getRequiredFields(@Nonnull PsiClass psiClass, boolean ignoreNonNull) {
    Collection<PsiField> result = new ArrayList<>();
    final boolean classAnnotatedWithValue = PsiAnnotationSearchUtil.isAnnotatedWith(psiClass, LombokClassNames.VALUE);

    for (PsiField psiField : getAllNotInitializedAndNotStaticFields(psiClass)) {
      final PsiModifierList modifierList = psiField.getModifierList();
      if (null != modifierList) {
        final boolean isFinal = isFieldFinal(psiField, modifierList, classAnnotatedWithValue);
        final boolean isNonNull = !ignoreNonNull && PsiAnnotationSearchUtil.isAnnotatedWith(psiField, LombokUtils.NONNULL_ANNOTATIONS);
        // accept initialized final or nonnull fields
        if ((isFinal || isNonNull) && !psiField.hasInitializer()) {
          result.add(psiField);
        }
      }
    }
    return result;
  }

  private static boolean isFieldFinal(@Nonnull PsiField psiField, @Nonnull PsiModifierList modifierList, boolean classAnnotatedWithValue) {
    boolean isFinal = modifierList.hasModifierProperty(PsiModifier.FINAL);
    if (!isFinal && classAnnotatedWithValue) {
      isFinal = PsiAnnotationSearchUtil.isNotAnnotatedWith(psiField, LombokClassNames.NON_FINAL);
    }
    return isFinal;
  }

  @Nonnull
  protected Collection<PsiMethod> createConstructorMethod(@Nonnull PsiClass psiClass,
                                                          @PsiModifier.ModifierConstant @Nonnull String methodModifier,
                                                          @Nonnull PsiAnnotation psiAnnotation,
                                                          boolean useJavaDefaults,
                                                          @Nonnull Collection<PsiField> params) {
    final String staticName = getStaticConstructorName(psiAnnotation);

    return createConstructorMethod(psiClass, methodModifier, psiAnnotation, useJavaDefaults, params, staticName, false);
  }

  protected String getStaticConstructorName(@Nonnull PsiAnnotation psiAnnotation) {
    return PsiAnnotationUtil.getStringAnnotationValue(psiAnnotation, "staticName", "");
  }

  private static boolean isStaticConstructor(@Nullable String staticName) {
    return !StringUtil.isEmptyOrSpaces(staticName);
  }

  @Nonnull
  protected Collection<PsiMethod> createConstructorMethod(@Nonnull PsiClass psiClass,
                                                          @PsiModifier.ModifierConstant @Nonnull String methodModifier,
                                                          @Nonnull PsiAnnotation psiAnnotation, boolean useJavaDefaults,
                                                          @Nonnull Collection<PsiField> params, @Nullable String staticName,
                                                          boolean skipConstructorIfAnyConstructorExists) {
    List<PsiMethod> methods = new ArrayList<>();

    boolean hasStaticConstructor = !validateIsStaticConstructorNotDefined(psiClass, staticName, params, new ProblemProcessingSink());

    final boolean staticConstructorRequired = isStaticConstructor(staticName);

    final String constructorVisibility = staticConstructorRequired || psiClass.isEnum() ? PsiModifier.PRIVATE : methodModifier;

    // check, if we should skip verification for presence of any (not Tolerated) constructors
    if (!skipConstructorIfAnyConstructorExists || !isAnyConstructorDefined(psiClass)) {
      boolean hasConstructor = !validateIsConstructorNotDefined(psiClass,
                                                                useJavaDefaults ? Collections.emptyList() : params,
                                                                new ProblemProcessingSink());
      if (!hasConstructor) {
        final PsiMethod constructor = createConstructor(psiClass, constructorVisibility, useJavaDefaults, params, psiAnnotation);
        methods.add(constructor);
      }
    }

    if (staticConstructorRequired && !hasStaticConstructor) {
      PsiMethod staticConstructor = createStaticConstructor(psiClass, methodModifier, staticName, useJavaDefaults, params, psiAnnotation);
      methods.add(staticConstructor);
    }

    return methods;
  }

  private static boolean isAnyConstructorDefined(@Nonnull PsiClass psiClass) {
    Collection<PsiMethod> constructors = PsiClassUtil.collectClassConstructorIntern(psiClass);
    return ContainerUtil.exists(constructors,
                                psiMethod -> PsiAnnotationSearchUtil.isNotAnnotatedWith(psiMethod, LombokClassNames.TOLERATE));
  }

  private PsiMethod createConstructor(@Nonnull PsiClass psiClass, @PsiModifier.ModifierConstant @Nonnull String modifier,
                                      boolean useJavaDefaults, @Nonnull Collection<PsiField> params, @Nonnull PsiAnnotation psiAnnotation) {
    LombokLightMethodBuilder constructorBuilder = new LombokLightMethodBuilder(psiClass.getManager(), getConstructorName(psiClass))
      .withConstructor(true)
      .withContainingClass(psiClass)
      .withNavigationElement(psiAnnotation)
      .withModifier(modifier);

    LombokCopyableAnnotations.copyOnXAnnotations(psiAnnotation, constructorBuilder.getModifierList(), "onConstructor");

    if (useJavaDefaults) {
      final StringBuilder blockText = new StringBuilder();

      for (PsiField param : params) {
        final String fieldInitializer = PsiTypesUtil.getDefaultValueOfType(param.getType());
        blockText.append(String.format("this.%s = %s;\n", param.getName(), fieldInitializer));
      }
      constructorBuilder.withBodyText(blockText.toString());
    }
    else {
      final List<String> fieldNames = new ArrayList<>();
      final AccessorsInfo.AccessorsValues classAccessorsValues = AccessorsInfo.getAccessorsValues(psiClass);
      for (PsiField psiField : params) {
        final AccessorsInfo paramAccessorsInfo = AccessorsInfo.buildFor(psiField, classAccessorsValues);
        fieldNames.add(paramAccessorsInfo.removePrefix(psiField.getName()));
      }

      if (!fieldNames.isEmpty()) {
        boolean addConstructorProperties =
          configDiscovery.getBooleanLombokConfigProperty(ConfigKey.ANYCONSTRUCTOR_ADD_CONSTRUCTOR_PROPERTIES, psiClass);
        if (addConstructorProperties ||
            !configDiscovery.getBooleanLombokConfigProperty(ConfigKey.ANYCONSTRUCTOR_SUPPRESS_CONSTRUCTOR_PROPERTIES, psiClass)) {
          final String constructorPropertiesAnnotation = "java.beans.ConstructorProperties( {" +
                                                         fieldNames.stream().collect(Collectors.joining("\", \"", "\"", "\"")) +
                                                         "} ) ";
          constructorBuilder.withAnnotation(constructorPropertiesAnnotation);
        }
      }

      final StringBuilder blockText = new StringBuilder();

      final Iterator<String> fieldNameIterator = fieldNames.iterator();
      final Iterator<PsiField> fieldIterator = params.iterator();
      while (fieldNameIterator.hasNext() && fieldIterator.hasNext()) {
        final String parameterName = fieldNameIterator.next();
        final PsiField parameterField = fieldIterator.next();

        final LombokLightParameter parameter = new LombokLightParameter(parameterName, parameterField.getType(), constructorBuilder);
        parameter.setNavigationElement(parameterField);
        constructorBuilder.withParameter(parameter);
        LombokCopyableAnnotations.copyCopyableAnnotations(parameterField, parameter.getModifierList(),
                                                          LombokCopyableAnnotations.BASE_COPYABLE);

        blockText.append(String.format("this.%s = %s;\n", parameterField.getName(), parameterName));
      }

      constructorBuilder.withBodyText(blockText.toString());
    }

    return constructorBuilder;
  }

  private static PsiMethod createStaticConstructor(PsiClass psiClass,
                                                   @PsiModifier.ModifierConstant String methodModifier,
                                                   String staticName,
                                                   boolean useJavaDefaults,
                                                   Collection<PsiField> params,
                                                   PsiAnnotation psiAnnotation) {
    LombokLightMethodBuilder methodBuilder = new LombokLightMethodBuilder(psiClass.getManager(), staticName)
      .withContainingClass(psiClass)
      .withNavigationElement(psiAnnotation)
      .withModifier(methodModifier)
      .withModifier(PsiModifier.STATIC);

    PsiSubstitutor substitutor = PsiSubstitutor.EMPTY;
    if (psiClass.hasTypeParameters()) {
      final PsiTypeParameter[] classTypeParameters = psiClass.getTypeParameters();

      // need to create new type parameters
      for (int index = 0; index < classTypeParameters.length; index++) {
        final PsiTypeParameter classTypeParameter = classTypeParameters[index];
        final LightTypeParameterBuilder methodTypeParameter = createTypeParameter(methodBuilder, index, classTypeParameter);
        methodBuilder.withTypeParameter(methodTypeParameter);

        substitutor = substitutor.put(classTypeParameter, PsiSubstitutor.EMPTY.substitute(methodTypeParameter));
      }
    }

    final PsiElementFactory factory = JavaPsiFacade.getElementFactory(psiClass.getProject());
    final PsiType returnType = factory.createType(psiClass, substitutor);
    methodBuilder.withMethodReturnType(returnType);

    if (!useJavaDefaults) {
      for (PsiField psiField : params) {
        final String parameterName = psiField.getName();
        final PsiType parameterType = substitutor.substitute(psiField.getType());
        final LombokLightParameter parameter = new LombokLightParameter(parameterName, parameterType, methodBuilder);
        parameter.setNavigationElement(psiField);
        methodBuilder.withParameter(parameter);
        LombokCopyableAnnotations.copyCopyableAnnotations(psiField, parameter.getModifierList(), LombokCopyableAnnotations.BASE_COPYABLE);
      }
    }

    final String codeBlockText = createStaticCodeBlockText(returnType, useJavaDefaults, methodBuilder.getParameterList());
    methodBuilder.withBodyText(codeBlockText);

    LombokAddNullAnnotations.createRelevantNonNullAnnotation(psiClass, methodBuilder);

    return methodBuilder;
  }

  @Nonnull
  private static LightTypeParameterBuilder createTypeParameter(LombokLightMethodBuilder method,
                                                               int index,
                                                               PsiTypeParameter psiClassTypeParameter) {
    final String nameOfTypeParameter = StringUtil.notNullize(psiClassTypeParameter.getName());

    final LightTypeParameterBuilder result = new LightTypeParameterBuilder(nameOfTypeParameter, method, index);
    final LightReferenceListBuilder resultExtendsList = result.getExtendsList();
    for (PsiClassType referencedType : psiClassTypeParameter.getExtendsList().getReferencedTypes()) {
      resultExtendsList.addReference(referencedType);
    }
    return result;
  }

  @Nonnull
  private static String createStaticCodeBlockText(@Nonnull PsiType psiType,
                                                  boolean useJavaDefaults,
                                                  @Nonnull final PsiParameterList parameterList) {
    final String psiClassName = psiType.getPresentableText();
    final String paramsText = useJavaDefaults ? "" : joinParameters(parameterList);
    return String.format("return new %s(%s);", psiClassName, paramsText);
  }

  private static String joinParameters(PsiParameterList parameterList) {
    return Arrays.stream(parameterList.getParameters()).map(PsiParameter::getName).collect(Collectors.joining(","));
  }
}
