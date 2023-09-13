package de.plushnikov.intellij.plugin.extension.postfix;

import com.intellij.java.impl.refactoring.introduceVariable.IntroduceVariableHandler;
import com.intellij.java.impl.refactoring.introduceVariable.IntroduceVariableSettings;
import com.intellij.java.impl.refactoring.ui.TypeSelectorManagerImpl;
import com.intellij.java.language.psi.PsiClassType;
import com.intellij.java.language.psi.PsiExpression;
import com.intellij.java.language.psi.PsiType;
import consulo.application.ApplicationManager;
import consulo.codeEditor.Editor;
import consulo.language.psi.PsiElement;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.project.Project;
import consulo.ui.ex.InputValidator;

public class IntroduceLombokVariableHandler extends IntroduceVariableHandler {
  private final String selectedTypeFQN;

  IntroduceLombokVariableHandler(String selectedTypeFQN) {
    this.selectedTypeFQN = selectedTypeFQN;
  }

  /*
   * This method with OccurrencesChooser.ReplaceChoice parameter exists up to 2017.2
   * Started from 2017.2 it use JavaReplaceChoice parameter
   */
  @Override
  public IntroduceVariableSettings getSettings(Project project, Editor editor, PsiExpression expr,
                                               PsiExpression[] occurrences, TypeSelectorManagerImpl typeSelectorManager,
                                               boolean declareFinalIfAll, boolean anyAssignmentLHS, InputValidator validator,
                                               PsiElement anchor, JavaReplaceChoice replaceChoice) {
    final IntroduceVariableSettings variableSettings;

    if (ApplicationManager.getApplication().isUnitTestMode()) {
      variableSettings = new UnitTestMockVariableSettings(expr);
    } else {
      variableSettings = super.getSettings(project, editor, expr, occurrences, typeSelectorManager, declareFinalIfAll,
        anyAssignmentLHS, validator, anchor, replaceChoice);
    }

    return getIntroduceVariableSettings(project, variableSettings);
  }

  private IntroduceVariableSettings getIntroduceVariableSettings(Project project, IntroduceVariableSettings variableSettings) {
    final PsiClassType psiClassType = PsiType.getTypeByName(selectedTypeFQN, project, GlobalSearchScope.projectScope(project));
    return new IntroduceVariableSettingsDelegate(variableSettings, psiClassType);
  }

  private static class UnitTestMockVariableSettings implements IntroduceVariableSettings {
    private final PsiExpression expr;

    UnitTestMockVariableSettings(PsiExpression expr) {
      this.expr = expr;
    }

    @Override
    public String getEnteredName() {
      return "foo";
    }

    @Override
    public boolean isReplaceAllOccurrences() {
      return false;
    }

    @Override
    public boolean isDeclareFinal() {
      return false;
    }

    @Override
    public boolean isReplaceLValues() {
      return false;
    }

    @Override
    public PsiType getSelectedType() {
      return expr.getType();
    }

    @Override
    public boolean isOK() {
      return true;
    }
  }
}
