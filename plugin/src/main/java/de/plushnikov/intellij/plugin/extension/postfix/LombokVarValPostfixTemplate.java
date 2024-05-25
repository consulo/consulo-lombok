package de.plushnikov.intellij.plugin.extension.postfix;

import com.intellij.java.impl.codeInsight.template.postfix.util.JavaPostfixTemplatesUtils;
import com.intellij.java.impl.refactoring.introduceVariable.IntroduceVariableHandler;
import com.intellij.java.language.psi.PsiExpression;
import consulo.codeEditor.Editor;
import consulo.language.editor.refactoring.postfixTemplate.PostfixTemplateWithExpressionSelector;
import consulo.language.psi.PsiElement;
import jakarta.annotation.Nonnull;

public class LombokVarValPostfixTemplate extends PostfixTemplateWithExpressionSelector {

  private final String selectedTypeFQN;

  LombokVarValPostfixTemplate(String name, String example, String selectedTypeFQN) {
    super(null, name, example,
          JavaPostfixTemplatesUtils.selectorAllExpressionsWithCurrentOffset(JavaPostfixTemplatesUtils.IS_NON_VOID),
          null);
    this.selectedTypeFQN = selectedTypeFQN;
  }

  @Override
  protected void expandForChooseExpression(@Nonnull PsiElement expression, @Nonnull Editor editor) {
    IntroduceVariableHandler handler = new IntroduceLombokVariableHandler(selectedTypeFQN);
    handler.invoke(expression.getProject(), editor, (PsiExpression) expression);
  }

}
