package de.plushnikov.intellij.plugin.provider;

import com.intellij.java.language.psi.PsiAnnotation;
import com.intellij.java.language.psi.PsiField;
import com.intellij.java.language.psi.PsiModifierListOwner;
import consulo.language.editor.ImplicitUsageProvider;
import consulo.language.psi.PsiElement;
import de.plushnikov.intellij.plugin.processor.LombokProcessorManager;
import de.plushnikov.intellij.plugin.processor.LombokPsiElementUsage;
import de.plushnikov.intellij.plugin.processor.Processor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

/**
 * Provides implicit usages of lombok fields
 */
public class LombokImplicitUsageProvider implements ImplicitUsageProvider {

  @Override
  public boolean isImplicitUsage(@NotNull PsiElement element) {
    return checkUsage(element, EnumSet.of(LombokPsiElementUsage.READ, LombokPsiElementUsage.WRITE, LombokPsiElementUsage.READ_WRITE));
  }

  @Override
  public boolean isImplicitRead(@NotNull PsiElement element) {
    return checkUsage(element, EnumSet.of(LombokPsiElementUsage.READ, LombokPsiElementUsage.READ_WRITE));
  }

  @Override
  public boolean isImplicitWrite(@NotNull PsiElement element) {
    return checkUsage(element, EnumSet.of(LombokPsiElementUsage.WRITE, LombokPsiElementUsage.READ_WRITE));
  }

  private static boolean checkUsage(@NotNull PsiElement element, EnumSet<LombokPsiElementUsage> elementUsages) {
    if (element instanceof PsiField psiField) {
      if (isUsedByLombokAnnotations(psiField, psiField, elementUsages) ||
          isUsedByLombokAnnotations(psiField, psiField.getContainingClass(), elementUsages)) {
        return true;
      }
    }
    return false;
  }

  private static boolean isUsedByLombokAnnotations(@NotNull PsiField psiField,
                                                   @Nullable PsiModifierListOwner modifierListOwner,
                                                   EnumSet<LombokPsiElementUsage> elementUsages) {
    if (null != modifierListOwner) {
      for (PsiAnnotation psiAnnotation : modifierListOwner.getAnnotations()) {
        for (Processor processor : LombokProcessorManager.getProcessors(psiAnnotation)) {
          final LombokPsiElementUsage psiElementUsage = processor.checkFieldUsage(psiField, psiAnnotation);
          if (elementUsages.contains(psiElementUsage)) {
            return true;
          }
        }
      }
    }
    return false;
  }
}
