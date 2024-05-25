package de.plushnikov.intellij.plugin.processor.modifier;

import com.intellij.java.language.psi.*;
import consulo.language.psi.PsiElement;
import consulo.language.psi.util.PsiTreeUtil;
import de.plushnikov.intellij.plugin.LombokClassNames;
import de.plushnikov.intellij.plugin.problem.ProblemValidationSink;
import de.plushnikov.intellij.plugin.processor.clazz.UtilityClassProcessor;
import de.plushnikov.intellij.plugin.util.PsiAnnotationSearchUtil;
import jakarta.annotation.Nonnull;

import java.util.Set;

/**
 * @author Florian Böhm
 */
public class UtilityClassModifierProcessor implements ModifierProcessor {

  public static boolean isModifierListSupported(@Nonnull PsiModifierList modifierList) {
    PsiElement modifierListParent = modifierList.getParent();

    if (modifierListParent instanceof PsiClass parentClass) {
      if (PsiAnnotationSearchUtil.isAnnotatedWith(parentClass, LombokClassNames.UTILITY_CLASS)) {
        return UtilityClassProcessor.validateOnRightType(parentClass, new ProblemValidationSink());
      }
    }

    if (!isElementFieldOrMethodOrInnerClass(modifierListParent)) {
      return false;
    }

    PsiClass searchableClass = PsiTreeUtil.getParentOfType(modifierListParent, PsiClass.class, true);

    return null != searchableClass && PsiAnnotationSearchUtil.isAnnotatedWith(searchableClass, LombokClassNames.UTILITY_CLASS) && UtilityClassProcessor.validateOnRightType(searchableClass, new ProblemValidationSink());
  }

  @Override
  public boolean isSupported(@Nonnull PsiModifierList modifierList) {
    return isModifierListSupported(modifierList);
  }

  @Override
  public void transformModifiers(@Nonnull PsiModifierList modifierList, @Nonnull final Set<String> modifiers) {
    final PsiElement parent = modifierList.getParent();

    // FINAL
    if (parent instanceof PsiClass psiClass) {
      if (PsiAnnotationSearchUtil.isAnnotatedWith(psiClass, LombokClassNames.UTILITY_CLASS)) {
        modifiers.add(PsiModifier.FINAL);
      }
    }

    // STATIC
    if (isElementFieldOrMethodOrInnerClass(parent)) {
      modifiers.add(PsiModifier.STATIC);
    }
  }

  private static boolean isElementFieldOrMethodOrInnerClass(PsiElement element) {
    return element instanceof PsiField || element instanceof PsiMethod ||
      (element instanceof PsiClass && element.getParent() instanceof PsiClass && !((PsiClass) element.getParent()).isInterface());
  }
}
