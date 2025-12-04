package de.plushnikov.intellij.plugin.handler;

import com.intellij.java.impl.ig.psiutils.InitializationUtils;
import com.intellij.java.language.psi.*;
import consulo.annotation.access.RequiredReadAction;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiReference;
import consulo.language.psi.util.PsiTreeUtil;
import de.plushnikov.intellij.plugin.LombokClassNames;
import de.plushnikov.intellij.plugin.util.PsiAnnotationSearchUtil;
import de.plushnikov.intellij.plugin.util.PsiAnnotationUtil;
import jakarta.annotation.Nonnull;

public final class LazyGetterHandler {
    public static boolean isLazyGetterHandled(@Nonnull PsiElement element) {
        if (!(element instanceof PsiIdentifier identifier)) {
            return false;
        }
        PsiField field = PsiTreeUtil.getParentOfType(identifier, PsiField.class);
        if (field == null) {
            return false;
        }

        PsiAnnotation getterAnnotation = PsiAnnotationSearchUtil.findAnnotation(field, LombokClassNames.GETTER);
        return null != getterAnnotation && PsiAnnotationUtil.getBooleanAnnotationValue(getterAnnotation, "lazy", false);
    }

    @RequiredReadAction
    public static boolean isInitializedInConstructors(@Nonnull PsiElement element) {
        if (!(element instanceof PsiIdentifier)) {
            return false;
        }
        if (!(element.getParent() instanceof PsiReferenceExpression refExpr)) {
            return false;
        }
        PsiElement qualifier = refExpr.getQualifier();
        if (qualifier == null) {
            return false;
        }
        PsiReference reference = qualifier.getReference();
        if (reference == null) {
            return false;
        }
        if (!(reference.resolve() instanceof PsiField field1)) {
            return false;
        }
        PsiClass containingClass = field1.getContainingClass();
        if (containingClass == null) {
            return false;
        }
        return InitializationUtils.isInitializedInConstructors(field1, containingClass);
    }
}
