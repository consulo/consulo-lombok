package de.plushnikov.intellij.plugin.inspection.modifiers;

import com.intellij.java.language.psi.PsiModifier;
import com.intellij.java.language.psi.PsiModifierListOwner;
import consulo.localize.LocalizeValue;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class RedundantModifiersInfo {
    private final RedundantModifiersInfoType myRedundantModifiersInfoType;
    private final String[] myModifiers;
    @Nonnull
    private final LocalizeValue myDescription;
    private final String myDontRunOnModifier;

    public RedundantModifiersInfo(
        @Nonnull RedundantModifiersInfoType redundantModifiersInfoType,
        @PsiModifier.ModifierConstant @Nullable String dontRunOnModifier,
        @Nonnull LocalizeValue description,
        @PsiModifier.ModifierConstant @Nonnull String... modifiers
    ) {
        this.myRedundantModifiersInfoType = redundantModifiersInfoType;
        this.myDescription = description;
        this.myDontRunOnModifier = dontRunOnModifier;
        this.myModifiers = modifiers;
    }

    @PsiModifier.ModifierConstant
    public String[] getModifiers() {
        return myModifiers;
    }

    @Nonnull
    public LocalizeValue getDescription() {
        return myDescription;
    }

    @PsiModifier.ModifierConstant
    public String getDontRunOnModifier() {
        return myDontRunOnModifier;
    }

    public RedundantModifiersInfoType getType() {
        return myRedundantModifiersInfoType;
    }

    public boolean shouldCheck(PsiModifierListOwner psiModifierListOwner) {
        return true;
    }
}
