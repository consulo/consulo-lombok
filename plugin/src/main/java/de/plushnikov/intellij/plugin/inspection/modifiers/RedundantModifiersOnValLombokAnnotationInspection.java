package de.plushnikov.intellij.plugin.inspection.modifiers;

import com.intellij.java.language.psi.PsiModifierListOwner;
import com.intellij.java.language.psi.PsiVariable;
import consulo.annotation.component.ExtensionImpl;
import consulo.localize.LocalizeValue;
import consulo.lombok.localize.LombokLocalize;
import de.plushnikov.intellij.plugin.processor.ValProcessor;
import jakarta.annotation.Nonnull;

import static com.intellij.java.language.psi.PsiModifier.FINAL;
import static de.plushnikov.intellij.plugin.inspection.modifiers.RedundantModifiersInfoType.VARIABLE;

@ExtensionImpl
public class RedundantModifiersOnValLombokAnnotationInspection extends LombokRedundantModifierInspection {
    public RedundantModifiersOnValLombokAnnotationInspection() {
        super(null, new RedundantModifiersInfo(VARIABLE, null, LombokLocalize.inspectionMessageValAlreadyMarksVariablesFinal(), FINAL) {
            @Override
            public boolean shouldCheck(PsiModifierListOwner psiModifierListOwner) {
                PsiVariable psiVariable = (PsiVariable) psiModifierListOwner;
                return ValProcessor.isVal(psiVariable);
            }
        });
    }

    @Nonnull
    @Override
    public LocalizeValue getDisplayName() {
        return LombokLocalize.inspectionRedundantModifiersValLombokDisplayName();
    }

    @Nonnull
    @Override
    public String getShortName() {
        return "RedundantModifiersValLombok";
    }
}
