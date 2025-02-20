package de.plushnikov.intellij.plugin.processor.field;

import com.intellij.java.language.psi.*;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.psi.PsiElement;
import consulo.util.lang.StringUtil;
import de.plushnikov.intellij.plugin.LombokClassNames;
import de.plushnikov.intellij.plugin.problem.ProblemSink;
import de.plushnikov.intellij.plugin.processor.LombokPsiElementUsage;
import de.plushnikov.intellij.plugin.psi.LombokLightMethodBuilder;
import de.plushnikov.intellij.plugin.psi.LombokLightModifierList;
import de.plushnikov.intellij.plugin.psi.LombokLightParameter;
import de.plushnikov.intellij.plugin.quickfix.PsiQuickFixFactory;
import de.plushnikov.intellij.plugin.thirdparty.LombokAddNullAnnotations;
import de.plushnikov.intellij.plugin.thirdparty.LombokCopyableAnnotations;
import de.plushnikov.intellij.plugin.thirdparty.LombokUtils;
import de.plushnikov.intellij.plugin.util.LombokProcessorUtil;
import de.plushnikov.intellij.plugin.util.PsiAnnotationSearchUtil;
import de.plushnikov.intellij.plugin.util.PsiClassUtil;
import jakarta.annotation.Nonnull;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Inspect and validate @Setter lombok annotation on a field
 * Creates setter method for this field
 *
 * @author Plushnikov Michail
 */
@ExtensionImpl(id = "SetterFieldProcessor", order = "after GetterFieldProcessor")
public final class SetterFieldProcessor extends AbstractFieldProcessor {
  public SetterFieldProcessor() {
    super(PsiMethod.class, LombokClassNames.SETTER);
  }

  @Override
  protected Collection<String> getNamesOfPossibleGeneratedElements(@Nonnull PsiClass psiClass,
                                                                   @Nonnull PsiAnnotation psiAnnotation,
                                                                   @Nonnull PsiField psiField) {
    final AccessorsInfo accessorsInfo = AccessorsInfo.buildFor(psiField);
    final String generatedElementName = LombokUtils.getSetterName(psiField, accessorsInfo);
    return Collections.singletonList(generatedElementName);
  }

  @Override
  protected void generatePsiElements(@Nonnull PsiField psiField,
                                     @Nonnull PsiAnnotation psiAnnotation,
                                     @Nonnull List<? super PsiElement> target) {
    final String methodVisibility = LombokProcessorUtil.getMethodModifier(psiAnnotation);
    final PsiClass psiClass = psiField.getContainingClass();
    if (methodVisibility != null && psiClass != null) {
      target.add(createSetterMethod(psiField, psiClass, methodVisibility));
    }
  }

  @Override
  protected boolean validate(@Nonnull PsiAnnotation psiAnnotation, @Nonnull PsiField psiField, @Nonnull ProblemSink builder) {
    boolean result;
    validateOnXAnnotations(psiAnnotation, psiField, builder, "onParam");

    result = validateFinalModifier(psiAnnotation, psiField, builder);
    if (result) {
      result = validateVisibility(psiAnnotation);
      if (result) {
        result = validateExistingMethods(psiField, builder, false);
        if (result) {
          result = validateAccessorPrefix(psiField, builder);
        }
      }
    }
    return result;
  }

  private static boolean validateFinalModifier(@Nonnull PsiAnnotation psiAnnotation,
                                               @Nonnull PsiField psiField,
                                               @Nonnull ProblemSink builder) {
    boolean result = true;
    if (psiField.hasModifierProperty(PsiModifier.FINAL) && null != LombokProcessorUtil.getMethodModifier(psiAnnotation)) {
      builder.addWarningMessage("inspection.message.not.generating.setter.for.this.field.setters")
        .withLocalQuickFixes(() -> PsiQuickFixFactory.createModifierListFix(psiField, PsiModifier.FINAL, false, false));
      result = false;
    }
    return result;
  }

  private static boolean validateVisibility(@Nonnull PsiAnnotation psiAnnotation) {
    final String methodVisibility = LombokProcessorUtil.getMethodModifier(psiAnnotation);
    return null != methodVisibility;
  }

  private static boolean validateAccessorPrefix(@Nonnull PsiField psiField, @Nonnull ProblemSink builder) {
    boolean result = true;
    if (AccessorsInfo.buildFor(psiField).isPrefixUnDefinedOrNotStartsWith(psiField.getName())) {
      builder.addWarningMessage("inspection.message.not.generating.setter.for.this.field.it");
      result = false;
    }
    return result;
  }

  public Collection<String> getAllSetterNames(@Nonnull PsiField psiField, boolean isBoolean) {
    final AccessorsInfo accessorsInfo = AccessorsInfo.buildFor(psiField);
    return LombokUtils.toAllSetterNames(accessorsInfo, psiField.getName(), isBoolean);
  }

  @Nonnull
  public static PsiMethod createSetterMethod(@Nonnull PsiField psiField, @Nonnull PsiClass psiClass, @Nonnull String methodModifier) {
    final String fieldName = psiField.getName();
    final PsiType psiFieldType = psiField.getType();
    final PsiAnnotation setterAnnotation = PsiAnnotationSearchUtil.findAnnotation(psiField, LombokClassNames.SETTER);

    final AccessorsInfo accessorsInfo = AccessorsInfo.buildFor(psiField);
    final String methodName = LombokUtils.getSetterName(psiField, accessorsInfo);

    PsiType returnType = getReturnType(psiField, accessorsInfo.isChain());
    LombokLightMethodBuilder methodBuilder = new LombokLightMethodBuilder(psiField.getManager(), methodName)
      .withMethodReturnType(returnType)
      .withContainingClass(psiClass)
      .withParameter(fieldName, psiFieldType)
      .withNavigationElement(psiField)
      .withContract("mutates=\"this\"");
    if (StringUtil.isNotEmpty(methodModifier)) {
      methodBuilder.withModifier(methodModifier);
    }
    boolean isStatic = psiField.hasModifierProperty(PsiModifier.STATIC);
    if (isStatic) {
      methodBuilder.withModifier(PsiModifier.STATIC);
    }
    if (accessorsInfo.isMakeFinal()) {
      methodBuilder.withModifier(PsiModifier.FINAL);
    }

    LombokLightParameter setterParameter = methodBuilder.getParameterList().getParameter(0);
    if (null != setterParameter) {
      LombokLightModifierList methodParameterModifierList = setterParameter.getModifierList();
      LombokCopyableAnnotations.copyCopyableAnnotations(psiField, methodParameterModifierList, LombokCopyableAnnotations.BASE_COPYABLE);
      LombokCopyableAnnotations.copyOnXAnnotations(setterAnnotation, methodParameterModifierList, "onParam");
    }

    final LombokLightModifierList modifierList = methodBuilder.getModifierList();
    LombokCopyableAnnotations.copyCopyableAnnotations(psiField, modifierList, LombokCopyableAnnotations.COPY_TO_SETTER);
    LombokCopyableAnnotations.copyOnXAnnotations(setterAnnotation, modifierList, "onMethod");
    if (psiField.isDeprecated()) {
      modifierList.addAnnotation(CommonClassNames.JAVA_LANG_DEPRECATED);
    }

    final String codeBlockText = createCodeBlockText(psiField, psiClass, returnType, isStatic, setterParameter);
    methodBuilder.withBodyText(codeBlockText);

    if (!PsiTypes.voidType().equals(returnType)) {
      LombokAddNullAnnotations.createRelevantNonNullAnnotation(psiClass, methodBuilder);
    }

    return methodBuilder;
  }

  @Nonnull
  private static String createCodeBlockText(@Nonnull PsiField psiField,
                                            @Nonnull PsiClass psiClass,
                                            PsiType returnType,
                                            boolean isStatic,
                                            PsiParameter methodParameter) {
    final String blockText;
    final String thisOrClass = isStatic ? psiClass.getName() : "this";
    blockText = String.format("%s.%s = %s; ", thisOrClass, psiField.getName(), methodParameter.getName());

    String codeBlockText = blockText;
    if (!isStatic && !PsiTypes.voidType().equals(returnType)) {
      codeBlockText += "return this;";
    }

    return codeBlockText;
  }

  private static PsiType getReturnType(@Nonnull PsiField psiField, boolean isChained) {
    PsiType result = PsiTypes.voidType();
    if (!psiField.hasModifierProperty(PsiModifier.STATIC) && isChained) {
      final PsiClass fieldClass = psiField.getContainingClass();
      if (null != fieldClass) {
        result = PsiClassUtil.getTypeWithGenerics(fieldClass);
      }
    }
    return result;
  }

  @Override
  public LombokPsiElementUsage checkFieldUsage(@Nonnull PsiField psiField, @Nonnull PsiAnnotation psiAnnotation) {
    return LombokPsiElementUsage.WRITE;
  }
}
