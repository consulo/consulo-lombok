package de.plushnikov.intellij.plugin.inspection.modifiers;

import consulo.annotation.component.ExtensionImpl;
import consulo.localize.LocalizeValue;
import consulo.lombok.localize.LombokLocalize;
import de.plushnikov.intellij.plugin.LombokClassNames;
import jakarta.annotation.Nonnull;

import static com.intellij.java.language.psi.PsiModifier.*;

/**
 * @author Rowicki Michał
 */
@ExtensionImpl
public class RedundantModifiersOnValueLombokAnnotationInspection extends LombokRedundantModifierInspection {

    public RedundantModifiersOnValueLombokAnnotationInspection() {
        super(
            LombokClassNames.VALUE,
            new RedundantModifiersInfo(
                RedundantModifiersInfoType.CLASS,
                null,
                LombokLocalize.inspectionMessageValueAlreadyMarksClassFinal(),
                FINAL
            ),
            new RedundantModifiersInfo(
                RedundantModifiersInfoType.FIELD,
                STATIC,
                LombokLocalize.inspectionMessageValueAlreadyMarksNonStaticFieldsFinal(),
                FINAL
            ),
            new RedundantModifiersInfo(
                RedundantModifiersInfoType.FIELD,
                STATIC,
                LombokLocalize.inspectionMessageValueAlreadyMarksNonStaticPackageLocalFieldsPrivate(),
                PRIVATE
            )
        );
    }

    @Nonnull
    @Override
    public String getShortName() {
        return "RedundantModifiersValueLombok";
    }

    @Nonnull
    @Override
    public LocalizeValue getDisplayName() {
        return LombokLocalize.inspectionRedundantModifiersValueLombokDisplayName();
    }
}
