package de.plushnikov.intellij.plugin.handler;

import com.intellij.java.impl.ig.psiutils.InitializationUtils;
import com.intellij.java.language.psi.*;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiReference;
import consulo.language.psi.util.PsiTreeUtil;
import de.plushnikov.intellij.plugin.LombokClassNames;
import de.plushnikov.intellij.plugin.util.PsiAnnotationSearchUtil;
import de.plushnikov.intellij.plugin.util.PsiAnnotationUtil;
import jakarta.annotation.Nonnull;

public final class LazyGetterHandler {

  public static boolean isLazyGetterHandled(@Nonnull PsiElement element) {
    if (!(element instanceof PsiIdentifier)) {
      return false;
    }
    PsiField field = PsiTreeUtil.getParentOfType(element, PsiField.class);
    if (field == null) {
      return false;
    }

    final PsiAnnotation getterAnnotation = PsiAnnotationSearchUtil.findAnnotation(field, LombokClassNames.GETTER);
    return null != getterAnnotation && PsiAnnotationUtil.getBooleanAnnotationValue(getterAnnotation, "lazy", false);
  }

  public static boolean isInitializedInConstructors(@Nonnull PsiElement element) {
    if (!(element instanceof PsiIdentifier)) {
      return false;
    }
    PsiElement parent = element.getParent();
    if (!(parent instanceof PsiReferenceExpression)) {
      return false;
    }
    PsiElement qualifier = ((PsiReferenceExpression) parent).getQualifier();
    if (qualifier == null) {
      return false;
    }
    PsiReference reference = qualifier.getReference();
    if (reference == null) {
      return false;
    }
    PsiElement field = reference.resolve();
    if (!(field instanceof PsiField)) {
      return false;
    }
    PsiClass containingClass = ((PsiField) field).getContainingClass();
    if (containingClass == null) {
      return false;
    }
    return InitializationUtils.isInitializedInConstructors((PsiField) field, containingClass);
  }
}
