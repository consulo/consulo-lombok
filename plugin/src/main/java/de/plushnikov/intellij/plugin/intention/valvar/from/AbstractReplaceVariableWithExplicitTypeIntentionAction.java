package de.plushnikov.intellij.plugin.intention.valvar.from;

import com.intellij.java.analysis.impl.codeInspection.RemoveRedundantTypeArgumentsUtil;
import com.intellij.java.language.psi.PsiDeclarationStatement;
import com.intellij.java.language.psi.PsiLocalVariable;
import com.intellij.java.language.psi.PsiTypeElement;
import com.intellij.java.language.psi.PsiVariable;
import com.intellij.java.language.psi.util.PsiTypesUtil;
import consulo.language.codeStyle.CodeStyleManager;
import consulo.language.psi.PsiElement;
import consulo.util.lang.StringUtil;
import de.plushnikov.intellij.plugin.LombokBundle;
import de.plushnikov.intellij.plugin.LombokClassNames;
import de.plushnikov.intellij.plugin.intention.valvar.AbstractValVarIntentionAction;
import de.plushnikov.intellij.plugin.processor.ValProcessor;
import org.jetbrains.annotations.Nls;
import jakarta.annotation.Nonnull;

public abstract class AbstractReplaceVariableWithExplicitTypeIntentionAction extends AbstractValVarIntentionAction {

  private final String variableClassName;

  public AbstractReplaceVariableWithExplicitTypeIntentionAction(String variableClassName) {
    this.variableClassName = variableClassName;
  }

  @Nls(capitalization = Nls.Capitalization.Sentence)
  @Nonnull
  @Override
  public String getText() {
    return LombokBundle.message("replace.0.with.explicit.type.lombok", StringUtil.getShortName(variableClassName));
  }

  @Override
  public boolean isAvailableOnVariable(PsiVariable psiVariable) {
    if (LombokClassNames.VAL.equals(variableClassName)) {
      return ValProcessor.isVal(psiVariable);
    }
    if (LombokClassNames.VAR.equals(variableClassName)) {
      return ValProcessor.isVar(psiVariable);
    }
    return false;
  }

  @Override
  public boolean isAvailableOnDeclarationStatement(PsiDeclarationStatement context) {
    if (context.getDeclaredElements().length <= 0) {
      return false;
    }
    PsiElement declaredElement = context.getDeclaredElements()[0];
    if (!(declaredElement instanceof PsiLocalVariable)) {
      return false;
    }
    return isAvailableOnVariable((PsiLocalVariable) declaredElement);
  }

  @Override
  public void invokeOnDeclarationStatement(PsiDeclarationStatement declarationStatement) {
    if (declarationStatement.getDeclaredElements().length > 0) {
      PsiElement declaredElement = declarationStatement.getDeclaredElements()[0];
      if (declaredElement instanceof PsiLocalVariable) {
        invokeOnVariable((PsiLocalVariable) declaredElement);
      }
    }
  }

  @Override
  public void invokeOnVariable(PsiVariable psiVariable) {
    PsiTypeElement psiTypeElement = psiVariable.getTypeElement();
    if (psiTypeElement == null) {
      return;
    }
    PsiTypesUtil.replaceWithExplicitType(psiTypeElement);
    RemoveRedundantTypeArgumentsUtil.removeRedundantTypeArguments(psiVariable);
    executeAfterReplacing(psiVariable);
    CodeStyleManager.getInstance(psiVariable.getProject()).reformat(psiVariable);
  }

  protected abstract void executeAfterReplacing(PsiVariable psiVariable);
}
