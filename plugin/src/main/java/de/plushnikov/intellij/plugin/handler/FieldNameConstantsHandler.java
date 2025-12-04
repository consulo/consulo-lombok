package de.plushnikov.intellij.plugin.handler;

import com.intellij.java.language.psi.PsiModifierListOwner;
import com.intellij.java.language.psi.PsiReferenceExpression;
import consulo.annotation.access.RequiredReadAction;
import consulo.language.psi.PsiElement;
import consulo.language.psi.util.PsiTreeUtil;
import de.plushnikov.intellij.plugin.LombokClassNames;
import de.plushnikov.intellij.plugin.util.PsiAnnotationSearchUtil;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public final class FieldNameConstantsHandler {
    @RequiredReadAction
    public static boolean isFiledNameConstants(@Nonnull PsiElement element) {
        @Nullable
        PsiReferenceExpression psiReferenceExpression = PsiTreeUtil.getParentOfType(element, PsiReferenceExpression.class);
        return psiReferenceExpression != null
            && psiReferenceExpression.resolve() instanceof PsiModifierListOwner modifierListOwner
            && PsiAnnotationSearchUtil.isAnnotatedWith(modifierListOwner, LombokClassNames.FIELD_NAME_CONSTANTS);
    }
}
