package de.plushnikov.intellij.plugin.handler;

import com.intellij.java.language.psi.PsiField;
import consulo.language.psi.PsiElement;
import consulo.language.psi.util.PsiTreeUtil;
import de.plushnikov.intellij.plugin.LombokClassNames;
import de.plushnikov.intellij.plugin.util.PsiAnnotationSearchUtil;
import jakarta.annotation.Nonnull;

public final class BuilderHandler {
    public static boolean isDefaultBuilderValue(@Nonnull PsiElement highlightedElement) {
        PsiField field = PsiTreeUtil.getParentOfType(highlightedElement, PsiField.class);
        return field != null && PsiAnnotationSearchUtil.isAnnotatedWith(field, LombokClassNames.BUILDER_DEFAULT);
    }
}
