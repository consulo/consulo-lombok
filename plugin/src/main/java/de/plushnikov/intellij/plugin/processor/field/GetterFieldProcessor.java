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
import de.plushnikov.intellij.plugin.quickfix.PsiQuickFixFactory;
import de.plushnikov.intellij.plugin.thirdparty.LombokCopyableAnnotations;
import de.plushnikov.intellij.plugin.thirdparty.LombokUtils;
import de.plushnikov.intellij.plugin.util.LombokProcessorUtil;
import de.plushnikov.intellij.plugin.util.PsiAnnotationSearchUtil;
import de.plushnikov.intellij.plugin.util.PsiAnnotationUtil;
import jakarta.annotation.Nonnull;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Inspect and validate @Getter lombok annotation on a field
 * Creates getter method for this field
 *
 * @author Plushnikov Michail
 */
@ExtensionImpl(id = "GetterFieldProcessor", order = "after DelegateFieldProcessor")
public final class GetterFieldProcessor extends AbstractFieldProcessor {
  public GetterFieldProcessor() {
    super(PsiMethod.class, LombokClassNames.GETTER);
  }

  @Override
  protected Collection<String> getNamesOfPossibleGeneratedElements(@Nonnull PsiClass psiClass,
                                                                   @Nonnull PsiAnnotation psiAnnotation,
                                                                   @Nonnull PsiField psiField) {
    final AccessorsInfo accessorsInfo = AccessorsInfo.buildFor(psiField);
    final String generatedElementName = LombokUtils.getGetterName(psiField, accessorsInfo);
    return Collections.singletonList(generatedElementName);
  }

  @Override
  protected void generatePsiElements(@Nonnull PsiField psiField,
                                     @Nonnull PsiAnnotation psiAnnotation,
                                     @Nonnull List<? super PsiElement> target) {
    final String methodVisibility = LombokProcessorUtil.getMethodModifier(psiAnnotation);
    final PsiClass psiClass = psiField.getContainingClass();
    if (null != methodVisibility && null != psiClass) {
      target.add(createGetterMethod(psiField, psiClass, methodVisibility));
    }
  }

  @Override
  protected boolean validate(@Nonnull PsiAnnotation psiAnnotation, @Nonnull PsiField psiField, @Nonnull ProblemSink builder) {
    boolean result;

    final String methodVisibility = LombokProcessorUtil.getMethodModifier(psiAnnotation);
    result = null != methodVisibility;

    final boolean lazy = isLazyGetter(psiAnnotation);
    if (null == methodVisibility && lazy) {
      builder.addWarningMessage("inspection.message.lazy.does.not.work.with.access.level.none");
    }

    if (result && lazy) {
      if (!psiField.hasModifierProperty(PsiModifier.FINAL) || !psiField.hasModifierProperty(PsiModifier.PRIVATE)) {
        builder.addErrorMessage("inspection.message.lazy.requires.field.to.be.private.final")
          .withLocalQuickFixes(()->PsiQuickFixFactory.createModifierListFix(psiField, PsiModifier.PRIVATE, true, false),
                               ()->PsiQuickFixFactory.createModifierListFix(psiField, PsiModifier.FINAL, true, false));
        result = false;
      }
      if (!psiField.hasInitializer()) {
        builder.addErrorMessage("inspection.message.lazy.requires.field.initialization");
        result = false;
      }
    }

    validateOnXAnnotations(psiAnnotation, psiField, builder, "onMethod");

    if (result) {
      result = validateExistingMethods(psiField, builder, true);
    }

    if (result) {
      result = validateAccessorPrefix(psiField, builder);
    }

    return result;
  }

  private static boolean isLazyGetter(@Nonnull PsiAnnotation psiAnnotation) {
    return PsiAnnotationUtil.getBooleanAnnotationValue(psiAnnotation, "lazy", false);
  }

  private static boolean validateAccessorPrefix(@Nonnull PsiField psiField, @Nonnull ProblemSink builder) {
    boolean result = true;
    if (AccessorsInfo.buildFor(psiField).isPrefixUnDefinedOrNotStartsWith(psiField.getName())) {
      builder.addWarningMessage("inspection.message.not.generating.getter.for.this.field");
      result = false;
    }
    return result;
  }

  @Nonnull
  public PsiMethod createGetterMethod(@Nonnull PsiField psiField, @Nonnull PsiClass psiClass, @Nonnull String methodModifier) {
    final AccessorsInfo accessorsInfo = AccessorsInfo.buildFor(psiField);
    final String methodName = LombokUtils.getGetterName(psiField, accessorsInfo);

    LombokLightMethodBuilder methodBuilder = new LombokLightMethodBuilder(psiField.getManager(), methodName)
      .withMethodReturnType(psiField.getType())
      .withContainingClass(psiClass)
      .withNavigationElement(psiField)
      .withContract("pure = true");
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

    final String blockText = String.format("return %s.%s;", isStatic ? psiClass.getName() : "this", psiField.getName());
    methodBuilder.withBodyText(blockText);

    final LombokLightModifierList modifierList = methodBuilder.getModifierList();

    LombokCopyableAnnotations.copyCopyableAnnotations(psiField, modifierList, LombokCopyableAnnotations.BASE_COPYABLE);
    PsiAnnotation fieldGetterAnnotation = PsiAnnotationSearchUtil.findAnnotation(psiField, LombokClassNames.GETTER);
    LombokCopyableAnnotations.copyOnXAnnotations(fieldGetterAnnotation, modifierList, "onMethod");
    if (psiField.isDeprecated()) {
      modifierList.addAnnotation(CommonClassNames.JAVA_LANG_DEPRECATED);
    }

    return methodBuilder;
  }

  @Override
  public LombokPsiElementUsage checkFieldUsage(@Nonnull PsiField psiField, @Nonnull PsiAnnotation psiAnnotation) {
    return LombokPsiElementUsage.READ;
  }
}
