package de.plushnikov.intellij.plugin.inspection.modifiers;

import com.intellij.java.language.psi.PsiModifier;
import com.intellij.java.language.psi.PsiModifierListOwner;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class RedundantModifiersInfo {

  private final RedundantModifiersInfoType redundantModifiersInfoType;
  private final String[] modifiers;
  private final String description;
  private final String dontRunOnModifier;

  public RedundantModifiersInfo(@Nonnull RedundantModifiersInfoType redundantModifiersInfoType,
                                @PsiModifier.ModifierConstant @Nullable String dontRunOnModifier,
                                @Nonnull String description,
                                @PsiModifier.ModifierConstant @Nonnull String... modifiers) {
    this.redundantModifiersInfoType = redundantModifiersInfoType;
    this.description = description;
    this.dontRunOnModifier = dontRunOnModifier;
    this.modifiers = modifiers;
  }

  @PsiModifier.ModifierConstant
  public String[] getModifiers() {
    return modifiers;
  }

  public String getDescription() {
    return description;
  }

  @PsiModifier.ModifierConstant
  public String getDontRunOnModifier() {
    return dontRunOnModifier;
  }

  public RedundantModifiersInfoType getType() {
    return redundantModifiersInfoType;
  }

  public boolean shouldCheck(PsiModifierListOwner psiModifierListOwner) {
    return true;
  }
}
