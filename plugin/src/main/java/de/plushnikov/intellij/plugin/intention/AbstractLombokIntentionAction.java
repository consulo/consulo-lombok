package de.plushnikov.intellij.plugin.intention;

import consulo.codeEditor.Editor;
import consulo.language.editor.intention.PsiElementBaseIntentionAction;
import consulo.language.psi.PsiElement;
import consulo.project.Project;
import de.plushnikov.intellij.plugin.util.LombokLibraryUtil;
import jakarta.annotation.Nonnull;

public abstract class AbstractLombokIntentionAction extends PsiElementBaseIntentionAction {

  @Override
  public boolean isAvailable(@Nonnull Project project, Editor editor, @Nonnull PsiElement element) {
    return LombokLibraryUtil.hasLombokLibrary(project);
  }
}
