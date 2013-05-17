package com.intellij.lombok.codeInsight.quickFixes;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiModifier;
import com.intellij.psi.PsiModifierList;
import com.intellij.psi.PsiModifierListOwner;
import org.jetbrains.annotations.NotNull;

/**
 * @author VISTALL
 * @since 20:02/02.05.13
 */
public class RemoveModifierFix implements LocalQuickFix {
  private final PsiModifierListOwner myModifierListOwner;
  @PsiModifier.ModifierConstant
  private final String myModifier;

  public RemoveModifierFix(@NotNull PsiModifierListOwner modifierListOwner, @PsiModifier.ModifierConstant String modifier) {
    myModifierListOwner = modifierListOwner;
    myModifier = modifier;
  }

  @NotNull
  @Override
  public String getName() {
    return String.format("Remove '%s' modifier", myModifier);
  }

  @NotNull
  @Override
  public String getFamilyName() {
    return "Lombok";
  }

  @Override
  public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
    PsiModifierList modifierList = myModifierListOwner.getModifierList();
    if(modifierList == null) {
      return;
    }
    modifierList.setModifierProperty(myModifier, false);
  }
}
