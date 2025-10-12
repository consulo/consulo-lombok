package de.plushnikov.intellij.plugin.inspection.modifiers;

import com.intellij.java.impl.ig.fixes.RemoveModifierFix;
import com.intellij.java.language.psi.*;
import consulo.language.editor.inspection.ProblemHighlightType;
import consulo.language.editor.inspection.ProblemsHolder;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiElementVisitor;
import consulo.language.psi.util.PsiTreeUtil;
import de.plushnikov.intellij.plugin.inspection.LombokJavaInspectionBase;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.Arrays;
import java.util.Optional;

public abstract class LombokRedundantModifierInspection extends LombokJavaInspectionBase {
    private final String supportedAnnotation;
    private final RedundantModifiersInfo[] redundantModifiersInfo;

    public LombokRedundantModifierInspection(@Nullable String supportedAnnotation, RedundantModifiersInfo... redundantModifiersInfo) {
        this.supportedAnnotation = supportedAnnotation;
        this.redundantModifiersInfo = redundantModifiersInfo;
    }

    @Nonnull
    @Override
    protected PsiElementVisitor createVisitor(@Nonnull ProblemsHolder holder, boolean isOnTheFly) {
        return new LombokRedundantModifiersVisitor(holder);
    }

    private class LombokRedundantModifiersVisitor extends JavaElementVisitor {
        private final ProblemsHolder holder;

        LombokRedundantModifiersVisitor(ProblemsHolder holder) {
            this.holder = holder;
        }

        @Override
        public void visitClass(@Nonnull PsiClass aClass) {
            super.visitClass(aClass);

            this.visit(aClass);
        }

        @Override
        public void visitField(@Nonnull PsiField field) {
            super.visitField(field);

            this.visit(field);
        }

        @Override
        public void visitMethod(@Nonnull PsiMethod method) {
            super.visitMethod(method);

            this.visit(method);
        }

        @Override
        public void visitLocalVariable(@Nonnull PsiLocalVariable variable) {
            super.visitLocalVariable(variable);

            this.visit(variable);
        }

        @Override
        public void visitParameter(@Nonnull PsiParameter parameter) {
            super.visitParameter(parameter);

            this.visit(parameter);
        }

        private void visit(PsiModifierListOwner psiModifierListOwner) {
            for (RedundantModifiersInfo redundantModifiersInfo : redundantModifiersInfo) {
                RedundantModifiersInfoType infoType = redundantModifiersInfo.getType();
                PsiModifierListOwner parentModifierListOwner = PsiTreeUtil.getParentOfType(
                    psiModifierListOwner,
                    PsiModifierListOwner.class,
                    infoType != RedundantModifiersInfoType.CLASS && infoType != RedundantModifiersInfoType.VARIABLE
                );
                if (parentModifierListOwner == null) {
                    continue;
                }
                if (infoType == RedundantModifiersInfoType.VARIABLE
                    && !(parentModifierListOwner instanceof PsiLocalVariable || parentModifierListOwner instanceof PsiParameter)
                    || (infoType != RedundantModifiersInfoType.VARIABLE && !(parentModifierListOwner instanceof PsiClass))) {
                    continue;
                }
                if ((supportedAnnotation == null || parentModifierListOwner.hasAnnotation(supportedAnnotation)) &&
                    redundantModifiersInfo.getType().getSupportedClass().isAssignableFrom(psiModifierListOwner.getClass())) {
                    PsiModifierList psiModifierList = psiModifierListOwner.getModifierList();
                    if (psiModifierList == null ||
                        (redundantModifiersInfo.getDontRunOnModifier() != null
                            && psiModifierList.hasExplicitModifier(redundantModifiersInfo.getDontRunOnModifier()))) {
                        continue;
                    }
                    if (!redundantModifiersInfo.shouldCheck(psiModifierListOwner)) {
                        continue;
                    }
                    for (String modifier : redundantModifiersInfo.getModifiers()) {
                        if (psiModifierList.hasExplicitModifier(modifier)) {
                            final Optional<PsiElement> psiModifier = Arrays.stream(psiModifierList.getChildren())
                                .filter(psiElement -> modifier.equals(psiElement.getText()))
                                .findFirst();

                            psiModifier.ifPresent(
                                psiElement -> holder.newProblem(redundantModifiersInfo.getDescription())
                                    .range(psiElement)
                                    .highlightType(ProblemHighlightType.WARNING)
                                    .withFix(new RemoveModifierFix(modifier))
                                    .create()
                            );
                        }
                    }
                }
            }
        }
    }
}
