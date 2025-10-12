package de.plushnikov.intellij.plugin.inspection;

import com.intellij.java.language.psi.*;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.editor.inspection.ProblemHighlightType;
import consulo.language.editor.inspection.ProblemsHolder;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiElementVisitor;
import consulo.localize.LocalizeValue;
import consulo.lombok.localize.LombokLocalize;
import de.plushnikov.intellij.plugin.LombokBundle;
import de.plushnikov.intellij.plugin.LombokClassNames;
import de.plushnikov.intellij.plugin.lombokconfig.ConfigDiscovery;
import de.plushnikov.intellij.plugin.lombokconfig.ConfigKey;
import de.plushnikov.intellij.plugin.util.PsiAnnotationSearchUtil;
import jakarta.annotation.Nonnull;

import java.util.Collection;

/**
 * @author Plushnikov Michail
 */
@ExtensionImpl
public class SpringQualifierCopyableLombokAnnotationInspection extends LombokJavaInspectionBase {
    private static final String SPRING_QUALIFIER_FQN = "org.springframework.beans.factory.annotation.Qualifier";

    @Nonnull
    @Override
    public LocalizeValue getDisplayName() {
        return LombokLocalize.inspectionSpringqualifiercopyableLombokDisplayName();
    }

    @Nonnull
    @Override
    protected PsiElementVisitor createVisitor(@Nonnull final ProblemsHolder holder, final boolean isOnTheFly) {
        return new LombokElementVisitor(holder);
    }

    private static class LombokElementVisitor extends JavaElementVisitor {
        private final ProblemsHolder holder;

        LombokElementVisitor(ProblemsHolder holder) {
            this.holder = holder;
        }

        @Override
        public void visitAnnotation(final @Nonnull PsiAnnotation annotation) {
            if (annotation.hasQualifiedName(SPRING_QUALIFIER_FQN)) {

                PsiAnnotationOwner annotationOwner = annotation.getOwner();
                if (annotationOwner instanceof PsiModifierList) {
                    PsiElement annotationOwnerParent = ((PsiModifierList) annotationOwner).getParent();
                    if (annotationOwnerParent instanceof PsiField) {
                        PsiClass psiClass = ((PsiField) annotationOwnerParent).getContainingClass();
                        if (psiClass != null && PsiAnnotationSearchUtil.isAnnotatedWith(
                            psiClass,
                            LombokClassNames.REQUIRED_ARGS_CONSTRUCTOR,
                            LombokClassNames.ALL_ARGS_CONSTRUCTOR
                        )) {
                            Collection<String> configuredCopyableAnnotations =
                                ConfigDiscovery.getInstance()
                                    .getMultipleValueLombokConfigProperty(ConfigKey.COPYABLE_ANNOTATIONS, psiClass);

                            if (!configuredCopyableAnnotations.contains(SPRING_QUALIFIER_FQN)) {
                                holder.newProblem(LombokLocalize.inspectionMessageAnnotationNotLombokCopyable(SPRING_QUALIFIER_FQN))
                                    .range(annotation)
                                    .highlightType(ProblemHighlightType.WARNING)
                                    .create();
                            }
                        }
                    }
                }
            }
        }
    }
}
