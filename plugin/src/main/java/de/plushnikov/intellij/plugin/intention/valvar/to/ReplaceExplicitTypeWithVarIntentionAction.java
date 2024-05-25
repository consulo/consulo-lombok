package de.plushnikov.intellij.plugin.intention.valvar.to;

import com.intellij.java.language.psi.*;
import consulo.language.psi.PsiElement;
import de.plushnikov.intellij.plugin.LombokClassNames;
import jakarta.annotation.Nonnull;

import static com.intellij.java.language.psi.PsiModifier.FINAL;

public class ReplaceExplicitTypeWithVarIntentionAction extends AbstractReplaceExplicitTypeWithVariableIntentionAction {

  public ReplaceExplicitTypeWithVarIntentionAction() {
    super(LombokClassNames.VAR);
  }

  @Override
  protected boolean isAvailableOnDeclarationCustom(@Nonnull PsiDeclarationStatement declarationStatement, @Nonnull PsiLocalVariable localVariable) {
    return isNotFinal(localVariable);
  }

  @Override
  protected void executeAfterReplacing(PsiVariable psiVariable) {
  }

  @Override
  public boolean isAvailableOnVariable(@Nonnull PsiVariable psiVariable) {
    if (!(psiVariable instanceof PsiParameter psiParameter)) {
      return false;
    }
    PsiElement declarationScope = psiParameter.getDeclarationScope();
    if (!(declarationScope instanceof PsiForStatement) && !(declarationScope instanceof PsiForeachStatement)) {
      return false;
    }
    PsiTypeElement typeElement = psiParameter.getTypeElement();
    return typeElement != null && !typeElement.isInferredType() && isNotFinal(psiParameter);
  }

  private static boolean isNotFinal(@Nonnull PsiVariable variable) {
    PsiModifierList modifierList = variable.getModifierList();
    return modifierList == null || !modifierList.hasExplicitModifier(FINAL);
  }
}
