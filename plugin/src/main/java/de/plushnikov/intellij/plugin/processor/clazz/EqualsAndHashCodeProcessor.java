package de.plushnikov.intellij.plugin.processor.clazz;

import com.intellij.java.language.psi.*;
import com.intellij.java.language.psi.util.PsiTypesUtil;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.editor.inspection.LocalQuickFix;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiManager;
import de.plushnikov.intellij.plugin.LombokClassNames;
import de.plushnikov.intellij.plugin.lombokconfig.ConfigKey;
import de.plushnikov.intellij.plugin.problem.ProblemSink;
import de.plushnikov.intellij.plugin.processor.LombokPsiElementUsage;
import de.plushnikov.intellij.plugin.processor.handler.EqualsAndHashCodeToStringHandler;
import de.plushnikov.intellij.plugin.processor.handler.EqualsAndHashCodeToStringHandler.MemberInfo;
import de.plushnikov.intellij.plugin.psi.LombokLightMethodBuilder;
import de.plushnikov.intellij.plugin.psi.LombokLightParameter;
import de.plushnikov.intellij.plugin.quickfix.PsiQuickFixFactory;
import de.plushnikov.intellij.plugin.thirdparty.LombokAddNullAnnotations;
import de.plushnikov.intellij.plugin.thirdparty.LombokCopyableAnnotations;
import de.plushnikov.intellij.plugin.util.PsiAnnotationSearchUtil;
import de.plushnikov.intellij.plugin.util.PsiAnnotationUtil;
import de.plushnikov.intellij.plugin.util.PsiClassUtil;
import de.plushnikov.intellij.plugin.util.PsiMethodUtil;
import jakarta.annotation.Nonnull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

/**
 * Inspect and validate @EqualsAndHashCode lombok annotation on a class
 * Creates equals/hashcode method for fields of this class
 *
 * @author Plushnikov Michail
 */
@ExtensionImpl(id = "EqualsAndHashCodeProcessor", order = "after DataProcessor")
public final class EqualsAndHashCodeProcessor extends AbstractClassProcessor {
  private static final String EQUALS_METHOD_NAME = "equals";
  private static final String HASH_CODE_METHOD_NAME = "hashCode";
  private static final String CAN_EQUAL_METHOD_NAME = "canEqual";

  private static final String INCLUDE_ANNOTATION_METHOD = "replaces";
  private static final String EQUALSANDHASHCODE_INCLUDE = LombokClassNames.EQUALS_AND_HASHCODE_INCLUDE;
  private static final String EQUALSANDHASHCODE_EXCLUDE = LombokClassNames.EQUALS_AND_HASHCODE_EXCLUDE;

  public EqualsAndHashCodeProcessor() {
    super(PsiMethod.class, LombokClassNames.EQUALS_AND_HASHCODE);
  }

  @Override
  protected Collection<String> getNamesOfPossibleGeneratedElements(@Nonnull PsiClass psiClass, @Nonnull PsiAnnotation psiAnnotation) {
    return List.of(EQUALS_METHOD_NAME, HASH_CODE_METHOD_NAME, CAN_EQUAL_METHOD_NAME);
  }

  @Override
  protected boolean validate(@Nonnull PsiAnnotation psiAnnotation, @Nonnull PsiClass psiClass, @Nonnull ProblemSink problemSink) {
    validateAnnotationOnRightType(psiClass, problemSink);

    if (problemSink.success()) {
      validateExistingMethods(psiClass, problemSink);
    }

    if (problemSink.deepValidation()) {
      final Collection<String> excludeProperty = PsiAnnotationUtil.getAnnotationValues(psiAnnotation, "exclude", String.class);
      final Collection<String> ofProperty = PsiAnnotationUtil.getAnnotationValues(psiAnnotation, "of", String.class);

      if (!excludeProperty.isEmpty() && !ofProperty.isEmpty()) {
        problemSink.addWarningMessage("inspection.message.exclude.are.mutually.exclusive.exclude.parameter.will.be.ignored")
          .withLocalQuickFixes(() -> PsiQuickFixFactory.createChangeAnnotationParameterFix(psiAnnotation, "exclude", null));
      }
      else {
        validateExcludeParam(psiClass, problemSink, psiAnnotation, excludeProperty);
      }
      validateOfParam(psiClass, problemSink, psiAnnotation, ofProperty);

      validateCallSuperParamIntern(psiAnnotation, psiClass, problemSink);
      validateCallSuperParamForObject(psiAnnotation, psiClass, problemSink);
    }
    return problemSink.success();
  }

  private void validateCallSuperParamIntern(@Nonnull PsiAnnotation psiAnnotation,
                                            @Nonnull PsiClass psiClass,
                                            @Nonnull ProblemSink builder) {
    validateCallSuperParam(psiAnnotation, psiClass, builder,
                           () -> PsiQuickFixFactory.createChangeAnnotationParameterFix(psiAnnotation, "callSuper", "false"),
                           () -> PsiQuickFixFactory.createChangeAnnotationParameterFix(psiAnnotation, "callSuper", "true"));
  }

  void validateCallSuperParamExtern(@Nonnull PsiAnnotation psiAnnotation, @Nonnull PsiClass psiClass, @Nonnull ProblemSink builder) {
    validateCallSuperParam(psiAnnotation, psiClass, builder,
                           () -> PsiQuickFixFactory.createAddAnnotationQuickFix(psiClass, "lombok.EqualsAndHashCode", "callSuper = true"));
  }

  private void validateCallSuperParam(@Nonnull PsiAnnotation psiAnnotation,
                                      @Nonnull PsiClass psiClass,
                                      @Nonnull ProblemSink builder,
                                      Supplier<LocalQuickFix>... quickFixes) {
    final Boolean declaredBooleanAnnotationValue = PsiAnnotationUtil.getDeclaredBooleanAnnotationValue(psiAnnotation, "callSuper");
    if (null == declaredBooleanAnnotationValue) {
      final String configProperty = configDiscovery.getStringLombokConfigProperty(ConfigKey.EQUALSANDHASHCODE_CALL_SUPER, psiClass);
      if (!"CALL".equalsIgnoreCase(configProperty) &&
          !"SKIP".equalsIgnoreCase(configProperty) &&
          PsiClassUtil.hasSuperClass(psiClass) &&
          !hasOneOfMethodsDefined(psiClass)) {
        builder.addWarningMessage("inspection.message.generating.equals.hashcode.implementation").withLocalQuickFixes(quickFixes);
      }
    }
  }

  private static void validateCallSuperParamForObject(PsiAnnotation psiAnnotation, PsiClass psiClass, ProblemSink builder) {
    boolean callSuperProperty = PsiAnnotationUtil.getBooleanAnnotationValue(psiAnnotation, "callSuper", false);
    if (callSuperProperty && !PsiClassUtil.hasSuperClass(psiClass)) {
      builder.addErrorMessage("inspection.message.generating.equals.hashcode.with.super.call")
        .withLocalQuickFixes(() -> PsiQuickFixFactory.createChangeAnnotationParameterFix(psiAnnotation, "callSuper", "false"),
                             () -> PsiQuickFixFactory.createChangeAnnotationParameterFix(psiAnnotation, "callSuper", null));
    }
  }

  private static void validateAnnotationOnRightType(@Nonnull PsiClass psiClass, @Nonnull ProblemSink builder) {
    final boolean definedOnWrongType = psiClass.isAnnotationType() || psiClass.isInterface() || psiClass.isEnum();
    if (definedOnWrongType) {
      builder.addErrorMessage("inspection.message.equals.and.hashcode.only.supported.on.class.type");
      builder.markFailed();
    }
  }

  private static void validateExistingMethods(@Nonnull PsiClass psiClass, @Nonnull ProblemSink builder) {
    if (hasOneOfMethodsDefined(psiClass)) {
      builder.addWarningMessage("inspection.message.not.generating.equals.hashcode");
      builder.markFailed();
    }
  }

  private static boolean hasOneOfMethodsDefined(@Nonnull PsiClass psiClass) {
    final Collection<PsiMethod> classMethodsIntern = PsiClassUtil.collectClassMethodsIntern(psiClass);
    return PsiMethodUtil.hasMethodByName(classMethodsIntern, EQUALS_METHOD_NAME, 1) ||
           PsiMethodUtil.hasMethodByName(classMethodsIntern, HASH_CODE_METHOD_NAME, 0);
  }

  @Override
  protected void generatePsiElements(@Nonnull PsiClass psiClass,
                                     @Nonnull PsiAnnotation psiAnnotation,
                                     @Nonnull List<? super PsiElement> target) {
    target.addAll(createEqualAndHashCode(psiClass, psiAnnotation));
  }

  Collection<PsiMethod> createEqualAndHashCode(@Nonnull PsiClass psiClass, @Nonnull PsiAnnotation psiAnnotation) {
    if (hasOneOfMethodsDefined(psiClass)) {
      return Collections.emptyList();
    }

    final boolean shouldGenerateCanEqual = shouldGenerateCanEqual(psiClass);

    Collection<PsiMethod> result = new ArrayList<>(3);
    result.add(createEqualsMethod(psiClass, psiAnnotation, shouldGenerateCanEqual));

    final Collection<PsiMethod> classMethods = PsiClassUtil.collectClassMethodsIntern(psiClass);
    if (shouldGenerateCanEqual && !PsiMethodUtil.hasMethodByName(classMethods, CAN_EQUAL_METHOD_NAME, 1)) {
      result.add(createCanEqualMethod(psiClass, psiAnnotation));
    }

    result.add(createHashCodeMethod(psiClass, psiAnnotation));
    return result;
  }

  private static boolean shouldGenerateCanEqual(@Nonnull PsiClass psiClass) {
    final boolean isNotDirectDescendantOfObject = PsiClassUtil.hasSuperClass(psiClass);
    if (isNotDirectDescendantOfObject) {
      return true;
    }

    final boolean isFinal = psiClass.hasModifierProperty(PsiModifier.FINAL) ||
                            (PsiAnnotationSearchUtil.isAnnotatedWith(psiClass, LombokClassNames.VALUE) &&
                             PsiAnnotationSearchUtil.isNotAnnotatedWith(psiClass, LombokClassNames.NON_FINAL));
    return !isFinal;
  }

  @Nonnull
  private PsiMethod createEqualsMethod(@Nonnull PsiClass psiClass,
                                       @Nonnull PsiAnnotation psiAnnotation,
                                       boolean hasCanEqualMethod) {
    final PsiManager psiManager = psiClass.getManager();

    final LombokLightMethodBuilder methodBuilder = new LombokLightMethodBuilder(psiManager, EQUALS_METHOD_NAME)
      .withModifier(PsiModifier.PUBLIC)
      .withMethodReturnType(PsiTypes.booleanType())
      .withContainingClass(psiClass)
      .withNavigationElement(psiAnnotation)
      .withFinalParameter("o", PsiType.getJavaLangObject(psiManager, psiClass.getResolveScope()));

    LombokLightParameter parameter = methodBuilder.getParameterList().getParameter(0);
    if (null != parameter) {
      LombokAddNullAnnotations.createRelevantNullableAnnotation(psiClass, parameter);
      copyOnXAnnotationsForFirstParam(psiAnnotation, parameter);
    }

    methodBuilder.withBodyText(m -> {
      PsiClass containingClass = m.getContainingClass();
      PsiAnnotation anno = (PsiAnnotation)m.getNavigationElement();
      return createEqualsBlockString(containingClass, anno, hasCanEqualMethod,
                                     EqualsAndHashCodeToStringHandler.filterMembers(containingClass, anno, true, INCLUDE_ANNOTATION_METHOD, null));
    });
    return methodBuilder;
  }

  private @Nonnull PsiMethod createHashCodeMethod(@Nonnull PsiClass psiClass,
                                                  @Nonnull PsiAnnotation psiAnnotation) {
    final PsiManager psiManager = psiClass.getManager();

    return new LombokLightMethodBuilder(psiManager, HASH_CODE_METHOD_NAME)
      .withModifier(PsiModifier.PUBLIC)
      .withMethodReturnType(PsiTypes.intType())
      .withContainingClass(psiClass)
      .withNavigationElement(psiAnnotation)
      .withBodyText(m -> {
        PsiClass containingClass = m.getContainingClass();
        PsiAnnotation anno = (PsiAnnotation)m.getNavigationElement();
        return createHashcodeBlockString(containingClass, anno,
                                         EqualsAndHashCodeToStringHandler.filterMembers(containingClass, anno, true, INCLUDE_ANNOTATION_METHOD,
                                                                                        null));
      });
  }

  @Nonnull
  private static PsiMethod createCanEqualMethod(@Nonnull PsiClass psiClass, @Nonnull PsiAnnotation psiAnnotation) {
    final PsiManager psiManager = psiClass.getManager();

    final String blockText = String.format("return other instanceof %s;", PsiTypesUtil.getClassType(psiClass).getCanonicalText());
    final LombokLightMethodBuilder methodBuilder = new LombokLightMethodBuilder(psiManager, CAN_EQUAL_METHOD_NAME)
      .withModifier(PsiModifier.PROTECTED)
      .withMethodReturnType(PsiTypes.booleanType())
      .withContainingClass(psiClass)
      .withNavigationElement(psiAnnotation)
      .withFinalParameter("other", PsiType.getJavaLangObject(psiManager, psiClass.getResolveScope()));

    LombokLightParameter parameter = methodBuilder.getParameterList().getParameter(0);
    if (null != parameter) {
      LombokAddNullAnnotations.createRelevantNullableAnnotation(psiClass, parameter);
      copyOnXAnnotationsForFirstParam(psiAnnotation, parameter);
    }

    methodBuilder.withBodyText(blockText);
    return methodBuilder;
  }

  private static void copyOnXAnnotationsForFirstParam(@Nonnull PsiAnnotation psiAnnotation, @Nonnull LombokLightParameter lightParameter) {
      PsiModifierList methodParameterModifierList = lightParameter.getModifierList();
      LombokCopyableAnnotations.copyOnXAnnotations(psiAnnotation, methodParameterModifierList, "onParam");
  }

  private @Nonnull String createEqualsBlockString(@Nonnull PsiClass psiClass,
                                                  @Nonnull PsiAnnotation psiAnnotation,
                                                  boolean hasCanEqualMethod,
                                                  Collection<MemberInfo> memberInfos) {
    final boolean callSuper = readCallSuperAnnotationOrConfigProperty(psiAnnotation, psiClass, ConfigKey.EQUALSANDHASHCODE_CALL_SUPER);
    final boolean doNotUseGetters =
      readAnnotationOrConfigProperty(psiAnnotation, psiClass, "doNotUseGetters", ConfigKey.EQUALSANDHASHCODE_DO_NOT_USE_GETTERS);

    final String canonicalClassName = PsiTypesUtil.getClassType(psiClass).getCanonicalText();
    final String canonicalWildcardClassName = PsiClassUtil.getWildcardClassType(psiClass).getCanonicalText();

    final StringBuilder builder = new StringBuilder();

    builder.append("if (o == this) return true;\n");
    builder.append("if (!(o instanceof ").append(canonicalClassName).append(")) return false;\n");
    builder.append("final ").append(canonicalWildcardClassName).append(" other = (").append(canonicalWildcardClassName).append(")o;\n");

    if (hasCanEqualMethod) {
      builder.append("if (!other.canEqual((java.lang.Object)this)) return false;\n");
    }
    if (callSuper) {
      builder.append("if (!super.equals(o)) return false;\n");
    }

    for (MemberInfo memberInfo : memberInfos) {
      final String memberAccessor = EqualsAndHashCodeToStringHandler.getMemberAccessorName(memberInfo, doNotUseGetters, psiClass);

      final PsiType memberType = memberInfo.getType();
      if (memberType instanceof PsiPrimitiveType) {
        if (PsiTypes.floatType().equals(memberType)) {
          builder.append("if (java.lang.Float.compare(this.").append(memberAccessor).append(", other.").append(memberAccessor)
            .append(") != 0) return false;\n");
        }
        else if (PsiTypes.doubleType().equals(memberType)) {
          builder.append("if (java.lang.Double.compare(this.").append(memberAccessor).append(", other.").append(memberAccessor)
            .append(") != 0) return false;\n");
        }
        else {
          builder.append("if (this.").append(memberAccessor).append(" != other.").append(memberAccessor).append(") return false;\n");
        }
      }
      else if (memberType instanceof PsiArrayType) {
        final PsiType componentType = ((PsiArrayType)memberType).getComponentType();
        if (componentType instanceof PsiPrimitiveType) {
          builder.append("if (!java.util.Arrays.equals(this.").append(memberAccessor).append(", other.").append(memberAccessor)
            .append(")) return false;\n");
        }
        else {
          builder.append("if (!java.util.Arrays.deepEquals(this.").append(memberAccessor).append(", other.").append(memberAccessor)
            .append(")) return false;\n");
        }
      }
      else {
        final String memberName = memberInfo.getName();
        builder.append("final java.lang.Object this$").append(memberName).append(" = this.").append(memberAccessor).append(";\n");
        builder.append("final java.lang.Object other$").append(memberName).append(" = other.").append(memberAccessor).append(";\n");
        builder.append("if (this$").append(memberName).append(" == null ? other$").append(memberName).append(" != null : !this$")
          .append(memberName).append(".equals(other$").append(memberName).append(")) return false;\n");
      }
    }
    builder.append("return true;\n");
    return builder.toString();
  }

  private static final int PRIME_FOR_HASHCODE = 59;
  private static final int PRIME_FOR_TRUE = 79;
  private static final int PRIME_FOR_FALSE = 97;
  private static final int PRIME_FOR_NULL = 43;

  @Nonnull
  private String createHashcodeBlockString(@Nonnull PsiClass psiClass,
                                           @Nonnull PsiAnnotation psiAnnotation,
                                           Collection<MemberInfo> memberInfos) {
    final boolean callSuper = readCallSuperAnnotationOrConfigProperty(psiAnnotation, psiClass, ConfigKey.EQUALSANDHASHCODE_CALL_SUPER);
    final boolean doNotUseGetters =
      readAnnotationOrConfigProperty(psiAnnotation, psiClass, "doNotUseGetters", ConfigKey.EQUALSANDHASHCODE_DO_NOT_USE_GETTERS);

    final StringBuilder builder = new StringBuilder();

    if (!memberInfos.isEmpty()) {
      builder.append("final int PRIME = ").append(PRIME_FOR_HASHCODE).append(";\n");
    }
    builder.append("int result = ");

    if (callSuper) {
      builder.append("super.hashCode();\n");
    }
    else {
      builder.append("1;\n");
    }

    for (MemberInfo memberInfo : memberInfos) {
      final String memberAccessor = EqualsAndHashCodeToStringHandler.getMemberAccessorName(memberInfo, doNotUseGetters, psiClass);
      final String memberName = memberInfo.getMethod() == null ? memberInfo.getName() : "$" + memberInfo.getName();

      final PsiType classFieldType = memberInfo.getType();
      if (classFieldType instanceof PsiPrimitiveType) {
        if (PsiTypes.booleanType().equals(classFieldType)) {
          builder.append("result = result * PRIME + (this.").append(memberAccessor).append(" ? ").append(PRIME_FOR_TRUE).append(" : ")
            .append(PRIME_FOR_FALSE).append(");\n");
        }
        else if (PsiTypes.longType().equals(classFieldType)) {
          builder.append("final long $").append(memberName).append(" = this.").append(memberAccessor).append(";\n");
          builder.append("result = result * PRIME + (int)($").append(memberName).append(" >>> 32 ^ $").append(memberName).append(");\n");
        }
        else if (PsiTypes.floatType().equals(classFieldType)) {
          builder.append("result = result * PRIME + java.lang.Float.floatToIntBits(this.").append(memberAccessor).append(");\n");
        }
        else if (PsiTypes.doubleType().equals(classFieldType)) {
          builder.append("final long $").append(memberName).append(" = java.lang.Double.doubleToLongBits(this.").append(memberAccessor)
            .append(");\n");
          builder.append("result = result * PRIME + (int)($").append(memberName).append(" >>> 32 ^ $").append(memberName).append(");\n");
        }
        else {
          builder.append("result = result * PRIME + this.").append(memberAccessor).append(";\n");
        }
      }
      else if (classFieldType instanceof PsiArrayType) {
        final PsiType componentType = ((PsiArrayType)classFieldType).getComponentType();
        if (componentType instanceof PsiPrimitiveType) {
          builder.append("result = result * PRIME + java.util.Arrays.hashCode(this.").append(memberAccessor).append(");\n");
        }
        else {
          builder.append("result = result * PRIME + java.util.Arrays.deepHashCode(this.").append(memberAccessor).append(");\n");
        }
      }
      else {
        builder.append("final java.lang.Object $").append(memberName).append(" = this.").append(memberAccessor).append(";\n");
        builder.append("result = result * PRIME + ($").append(memberName).append(" == null ? " + PRIME_FOR_NULL + " : $").append(memberName)
          .append(".hashCode());\n");
      }
    }
    builder.append("return result;\n");
    return builder.toString();
  }

  @Nonnull
  @Override
  public Collection<PsiAnnotation> collectProcessedAnnotations(@Nonnull PsiClass psiClass) {
    final Collection<PsiAnnotation> result = super.collectProcessedAnnotations(psiClass);
    addFieldsAnnotation(result, psiClass, EQUALSANDHASHCODE_INCLUDE, EQUALSANDHASHCODE_EXCLUDE);
    return result;
  }

  @Override
  public LombokPsiElementUsage checkFieldUsage(@Nonnull PsiField psiField, @Nonnull PsiAnnotation psiAnnotation) {
    final PsiClass containingClass = psiField.getContainingClass();
    if (null != containingClass) {
      final String psiFieldName = psiField.getName();
      if (EqualsAndHashCodeToStringHandler.filterMembers(containingClass, psiAnnotation, true, INCLUDE_ANNOTATION_METHOD, null).stream()
        .map(MemberInfo::getName).anyMatch(psiFieldName::equals)) {
        return LombokPsiElementUsage.READ;
      }
    }
    return LombokPsiElementUsage.NONE;
  }
}
