package de.plushnikov.intellij.plugin.inspection;

import com.intellij.java.language.psi.*;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.editor.inspection.ProblemHighlightType;
import consulo.language.editor.inspection.ProblemsHolder;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiElementVisitor;
import consulo.localize.LocalizeValue;
import consulo.lombok.localize.LombokLocalize;
import de.plushnikov.intellij.plugin.problem.LombokProblem;
import de.plushnikov.intellij.plugin.processor.LombokProcessorManager;
import de.plushnikov.intellij.plugin.processor.Processor;
import de.plushnikov.intellij.plugin.processor.ValProcessor;
import de.plushnikov.intellij.plugin.psi.LombokLightMethodBuilder;
import jakarta.annotation.Nonnull;

import java.util.Collection;
import java.util.HashSet;

/**
 * @author Plushnikov Michail
 */
@ExtensionImpl
public class LombokInspection extends LombokJavaInspectionBase {
    @Nonnull
    @Override
    public LocalizeValue getDisplayName() {
        return LombokLocalize.inspectionLombokDisplayName();
    }

    @Nonnull
    @Override
    protected PsiElementVisitor createVisitor(@Nonnull final ProblemsHolder holder, final boolean isOnTheFly) {
        return new LombokElementVisitor(holder);
    }

    private static class LombokElementVisitor extends JavaElementVisitor {
        private static final ValProcessor valProcessor = new ValProcessor();
        private final ProblemsHolder holder;

        LombokElementVisitor(ProblemsHolder holder) {
            this.holder = holder;
        }

        @Override
        public void visitLocalVariable(@Nonnull PsiLocalVariable variable) {
            super.visitLocalVariable(variable);

            valProcessor.verifyVariable(variable, holder);
        }

        @Override
        public void visitParameter(@Nonnull PsiParameter parameter) {
            super.visitParameter(parameter);

            valProcessor.verifyParameter(parameter, holder);
        }

        @Override
        public void visitAnnotation(@Nonnull PsiAnnotation annotation) {
            super.visitAnnotation(annotation);

            final Collection<LombokProblem> problems = new HashSet<>();

            for (Processor inspector : LombokProcessorManager.getProcessors(annotation)) {
                problems.addAll(inspector.verifyAnnotation(annotation));
            }

            for (LombokProblem problem : problems) {
                holder.newProblem(LocalizeValue.of(problem.getMessage()))
                    .range(annotation)
                    .highlightType(problem.getHighlightType())
                    .withFixes(problem.getQuickFixes())
                    .create();
            }
        }

        /**
         * Check MethodCallExpressions for calls for default (argument less) constructor
         * Produce an error if resolved constructor method is build by lombok and contains some arguments
         */
        @Override
        public void visitMethodCallExpression(@Nonnull PsiMethodCallExpression methodCall) {
            super.visitMethodCallExpression(methodCall);

            PsiExpressionList list = methodCall.getArgumentList();
            PsiReferenceExpression referenceToMethod = methodCall.getMethodExpression();

            boolean isThisOrSuper = referenceToMethod.getReferenceNameElement() instanceof PsiKeyword;
            final int parameterCount = list.getExpressions().length;
            if (isThisOrSuper && parameterCount == 0) {
                JavaResolveResult[] results = referenceToMethod.multiResolve(true);
                JavaResolveResult resolveResult = results.length == 1 ? results[0] : JavaResolveResult.EMPTY;
                PsiElement resolved = resolveResult.getElement();

                if (resolved instanceof LombokLightMethodBuilder lightMethodBuilder
                    && lightMethodBuilder.getParameterList().getParameters().length != 0) {
                    holder.newProblem(LombokLocalize.inspectionMessageDefaultConstructorDoesnTExist())
                        .range(methodCall)
                        .highlightType(ProblemHighlightType.ERROR)
                        .create();
                }
            }
        }
    }
}
