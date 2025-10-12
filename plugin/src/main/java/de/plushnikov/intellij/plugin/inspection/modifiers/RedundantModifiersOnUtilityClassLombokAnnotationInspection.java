package de.plushnikov.intellij.plugin.inspection.modifiers;

import consulo.annotation.component.ExtensionImpl;
import consulo.localize.LocalizeValue;
import consulo.lombok.localize.LombokLocalize;
import de.plushnikov.intellij.plugin.LombokClassNames;
import jakarta.annotation.Nonnull;

import static com.intellij.java.language.psi.PsiModifier.FINAL;
import static com.intellij.java.language.psi.PsiModifier.STATIC;
import static de.plushnikov.intellij.plugin.inspection.modifiers.RedundantModifiersInfoType.*;

@ExtensionImpl
public class RedundantModifiersOnUtilityClassLombokAnnotationInspection extends LombokRedundantModifierInspection {
    public RedundantModifiersOnUtilityClassLombokAnnotationInspection() {
        super(
            LombokClassNames.UTILITY_CLASS,
            new RedundantModifiersInfo(CLASS, null, LombokLocalize.inspectionMessageUtilityClassAlreadyMarksClassFinal(), FINAL),
            new RedundantModifiersInfo(FIELD, null, LombokLocalize.inspectionMessageUtilityClassAlreadyMarksFieldsStatic(), STATIC),
            new RedundantModifiersInfo(METHOD, null, LombokLocalize.inspectionMessageUtilityClassAlreadyMarksMethodsStatic(), STATIC),
            new RedundantModifiersInfo(
                INNER_CLASS,
                null,
                LombokLocalize.inspectionMessageUtilityClassAlreadyMarksInnerClassesStatic(),
                STATIC
            )
        );
    }

    @Nonnull
    @Override
    public String getShortName() {
        return "RedundantModifiersUtilityClassLombok";
    }

    @Nonnull
    @Override
    public LocalizeValue getDisplayName() {
        return LombokLocalize.inspectionRedundantModifiersUtilityClassLombokDisplayName();
    }
}
