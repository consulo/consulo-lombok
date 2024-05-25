package de.plushnikov.intellij.plugin.intention.valvar.from;

import com.intellij.java.language.psi.PsiModifier;
import com.intellij.java.language.psi.PsiModifierList;
import com.intellij.java.language.psi.PsiVariable;
import de.plushnikov.intellij.plugin.LombokClassNames;

public class ReplaceValWithExplicitTypeIntentionAction extends AbstractReplaceVariableWithExplicitTypeIntentionAction {

  public ReplaceValWithExplicitTypeIntentionAction() {
    super(LombokClassNames.VAL);
  }

  @Override
  protected void executeAfterReplacing(PsiVariable psiVariable) {
    PsiModifierList psiModifierList = psiVariable.getModifierList();
    if (psiModifierList != null) {
      psiModifierList.setModifierProperty(PsiModifier.FINAL, true);
    }
  }
}
