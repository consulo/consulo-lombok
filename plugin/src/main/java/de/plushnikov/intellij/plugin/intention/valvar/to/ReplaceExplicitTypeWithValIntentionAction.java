package de.plushnikov.intellij.plugin.intention.valvar.to;

import com.intellij.java.language.psi.*;
import de.plushnikov.intellij.plugin.LombokClassNames;
import jakarta.annotation.Nonnull;

import static com.intellij.java.language.psi.PsiModifier.FINAL;

public class ReplaceExplicitTypeWithValIntentionAction extends AbstractReplaceExplicitTypeWithVariableIntentionAction {
    public ReplaceExplicitTypeWithValIntentionAction() {
        super(LombokClassNames.VAL);
    }

    @Override
    protected boolean isAvailableOnDeclarationCustom(
        @Nonnull PsiDeclarationStatement declarationStatement,
        @Nonnull PsiLocalVariable localVariable
    ) {
        return !(declarationStatement.getParent() instanceof PsiForStatement);
    }

    @Override
    protected void executeAfterReplacing(PsiVariable psiVariable) {
        PsiModifierList modifierList = psiVariable.getModifierList();
        if (modifierList != null) {
            modifierList.setModifierProperty(FINAL, false);
        }
    }

    @Override
    public boolean isAvailableOnVariable(PsiVariable psiVariable) {
        if (!(psiVariable instanceof PsiParameter parameter)) {
            return false;
        }
        if (!(parameter.getDeclarationScope() instanceof PsiForeachStatement)) {
            return false;
        }
        PsiTypeElement typeElement = parameter.getTypeElement();
        return typeElement == null || !typeElement.isInferredType();
    }
}
