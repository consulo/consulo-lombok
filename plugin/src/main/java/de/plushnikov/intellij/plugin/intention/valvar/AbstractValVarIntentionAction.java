package de.plushnikov.intellij.plugin.intention.valvar;

import com.intellij.java.language.JavaLanguage;
import com.intellij.java.language.psi.*;
import consulo.codeEditor.Editor;
import consulo.language.editor.intention.LowPriorityAction;
import consulo.language.psi.PsiCompiledElement;
import consulo.language.psi.PsiElement;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.project.Project;
import de.plushnikov.intellij.plugin.intention.AbstractLombokIntentionAction;
import jakarta.annotation.Nonnull;

public abstract class AbstractValVarIntentionAction extends AbstractLombokIntentionAction implements LowPriorityAction {

  @Override
  public boolean isAvailable(@Nonnull Project project, Editor editor, @Nonnull PsiElement element) {
    if (!super.isAvailable(project, editor, element)) {
      return false;
    }
    if (element instanceof PsiCompiledElement || !canModify(element) || !element.getLanguage().is(JavaLanguage.INSTANCE)) {
      return false;
    }

    PsiParameter parameter = PsiTreeUtil.getParentOfType(element, PsiParameter.class, false, PsiClass.class, PsiCodeBlock.class);
    if (parameter != null) {
      return isAvailableOnVariable(parameter);
    }
    PsiDeclarationStatement
      context = PsiTreeUtil.getParentOfType(element, PsiDeclarationStatement.class, false, PsiClass.class, PsiCodeBlock.class);
    return context != null && isAvailableOnDeclarationStatement(context);
  }

  @Override
  public void invoke(@Nonnull Project project, Editor editor, @Nonnull PsiElement element) {
    final PsiDeclarationStatement declarationStatement = PsiTreeUtil.getParentOfType(element, PsiDeclarationStatement.class);

    if (declarationStatement != null) {
      invokeOnDeclarationStatement(declarationStatement);
      return;
    }

    final PsiParameter parameter = PsiTreeUtil.getParentOfType(element, PsiParameter.class);
    if (parameter != null) {
      invokeOnVariable(parameter);
    }
  }

  public abstract boolean isAvailableOnVariable(PsiVariable psiVariable);

  public abstract boolean isAvailableOnDeclarationStatement(PsiDeclarationStatement psiDeclarationStatement);

  public abstract void invokeOnVariable(PsiVariable psiVariable);

  public abstract void invokeOnDeclarationStatement(PsiDeclarationStatement psiDeclarationStatement);
}
