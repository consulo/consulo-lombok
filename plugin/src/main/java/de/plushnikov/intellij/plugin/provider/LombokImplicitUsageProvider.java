package de.plushnikov.intellij.plugin.provider;

import com.intellij.java.language.psi.PsiAnnotation;
import com.intellij.java.language.psi.PsiField;
import com.intellij.java.language.psi.PsiModifierListOwner;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.editor.ImplicitUsageProvider;
import consulo.language.psi.PsiElement;
import de.plushnikov.intellij.plugin.processor.LombokProcessorManager;
import de.plushnikov.intellij.plugin.processor.LombokPsiElementUsage;
import de.plushnikov.intellij.plugin.processor.Processor;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.EnumSet;

/**
 * Provides implicit usages of lombok fields
 */
@ExtensionImpl
public class LombokImplicitUsageProvider implements ImplicitUsageProvider {

  @Override
  public boolean isImplicitUsage(@Nonnull PsiElement element) {
    return checkUsage(element, EnumSet.of(LombokPsiElementUsage.READ, LombokPsiElementUsage.WRITE, LombokPsiElementUsage.READ_WRITE));
  }

  @Override
  public boolean isImplicitRead(@Nonnull PsiElement element) {
    return checkUsage(element, EnumSet.of(LombokPsiElementUsage.READ, LombokPsiElementUsage.READ_WRITE));
  }

  @Override
  public boolean isImplicitWrite(@Nonnull PsiElement element) {
    return checkUsage(element, EnumSet.of(LombokPsiElementUsage.WRITE, LombokPsiElementUsage.READ_WRITE));
  }

  private static boolean checkUsage(@Nonnull PsiElement element, EnumSet<LombokPsiElementUsage> elementUsages) {
    if (element instanceof PsiField psiField) {
      if (isUsedByLombokAnnotations(psiField, psiField, elementUsages) ||
          isUsedByLombokAnnotations(psiField, psiField.getContainingClass(), elementUsages)) {
        return true;
      }
    }
    return false;
  }

  private static boolean isUsedByLombokAnnotations(@Nonnull PsiField psiField,
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
