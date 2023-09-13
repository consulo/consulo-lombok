package de.plushnikov.intellij.plugin.extension.postfix;

import com.intellij.java.impl.codeInsight.template.postfix.util.JavaPostfixTemplatesUtils;
import com.intellij.java.impl.refactoring.introduceVariable.IntroduceVariableHandler;
import com.intellij.java.language.psi.PsiExpression;
import consulo.codeEditor.Editor;
import consulo.language.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

public class LombokVarValPostfixTemplate extends PostfixTemplateWithExpressionSelector {

  private final String selectedTypeFQN;

  LombokVarValPostfixTemplate(String name, String example, String selectedTypeFQN) {
    super(null, name, example,
          JavaPostfixTemplatesUtils.selectorAllExpressionsWithCurrentOffset(JavaPostfixTemplatesUtils.IS_NON_VOID),
          null);
    this.selectedTypeFQN = selectedTypeFQN;
  }

  @Override
  protected void expandForChooseExpression(@NotNull PsiElement expression, @NotNull Editor editor) {
    IntroduceVariableHandler handler = new IntroduceLombokVariableHandler(selectedTypeFQN);
    handler.invoke(expression.getProject(), editor, (PsiExpression) expression);
  }

}
