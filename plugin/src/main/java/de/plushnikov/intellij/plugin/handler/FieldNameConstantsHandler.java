package de.plushnikov.intellij.plugin.handler;

import com.intellij.java.language.psi.PsiModifierListOwner;
import com.intellij.java.language.psi.PsiReferenceExpression;
import consulo.language.psi.PsiElement;
import consulo.language.psi.util.PsiTreeUtil;
import de.plushnikov.intellij.plugin.LombokClassNames;
import de.plushnikov.intellij.plugin.util.PsiAnnotationSearchUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class FieldNameConstantsHandler {

  public static boolean isFiledNameConstants(@NotNull PsiElement element) {
    @Nullable PsiReferenceExpression psiReferenceExpression = PsiTreeUtil.getParentOfType(element, PsiReferenceExpression.class);
    if (psiReferenceExpression == null) {
      return false;
    }
    PsiElement psiElement = psiReferenceExpression.resolve();
    if (!(psiElement instanceof PsiModifierListOwner)) {
      return false;
    }
    return PsiAnnotationSearchUtil.isAnnotatedWith((PsiModifierListOwner) psiElement, LombokClassNames.FIELD_NAME_CONSTANTS);
  }
}
