package de.plushnikov.intellij.plugin.quickfix;

import com.intellij.java.analysis.impl.codeInsight.intention.AddAnnotationFix;
import com.intellij.java.impl.codeInsight.daemon.impl.quickfix.ModifierFix;
import com.intellij.java.impl.ig.fixes.ChangeAnnotationParameterQuickFix;
import com.intellij.java.language.psi.*;
import consulo.language.editor.inspection.LocalQuickFix;
import consulo.util.lang.StringUtil;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * @author Plushnikov Michail
 */
public final class PsiQuickFixFactory {
  public static LocalQuickFix createAddAnnotationQuickFix(@Nonnull PsiClass psiClass, @Nonnull String annotationFQN, @Nullable String annotationParam) {
    PsiElementFactory elementFactory = JavaPsiFacade.getElementFactory(psiClass.getProject());
    PsiAnnotation newAnnotation = elementFactory.createAnnotationFromText("@" + annotationFQN + "(" + StringUtil.notNullize(annotationParam) + ")", psiClass);
    final PsiNameValuePair[] attributes = newAnnotation.getParameterList().getAttributes();

    return new AddAnnotationFix(annotationFQN, psiClass, attributes);
  }

  public static LocalQuickFix createModifierListFix(@Nonnull PsiModifierListOwner owner, @Nonnull String modifier, boolean shouldHave, final boolean showContainingClass) {
    return new ModifierFix(owner, modifier, shouldHave, showContainingClass);
  }

  public static LocalQuickFix createNewFieldFix(@Nonnull PsiClass psiClass, @Nonnull String name, @Nonnull PsiType psiType, @Nullable String initializerText, String... modifiers) {
    return new CreateFieldQuickFix(psiClass, name, psiType, initializerText, modifiers);
  }

  public static LocalQuickFix createChangeAnnotationParameterFix(@Nonnull PsiAnnotation psiAnnotation, @Nonnull String name, @Nullable String newValue) {
    return new ChangeAnnotationParameterQuickFix(psiAnnotation, name, newValue);
  }
}
