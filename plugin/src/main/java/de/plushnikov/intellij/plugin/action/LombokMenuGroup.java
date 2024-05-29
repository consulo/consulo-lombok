package de.plushnikov.intellij.plugin.action;

import com.intellij.java.language.psi.PsiJavaFile;
import consulo.application.dumb.DumbAware;
import consulo.language.editor.CommonDataKeys;
import consulo.lombok.impl.icon.LombokIconGroup;
import consulo.project.Project;
import consulo.ui.ex.action.AnActionEvent;
import consulo.ui.ex.action.DefaultActionGroup;
import consulo.ui.image.Image;
import de.plushnikov.intellij.plugin.util.LombokLibraryUtil;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class LombokMenuGroup extends DefaultActionGroup implements DumbAware {

//  @Override
//  public @NotNull ActionUpdateThread getActionUpdateThread() {
//    return ActionUpdateThread.BGT;
//  }


  @Nullable
  @Override
  protected Image getTemplateIcon() {
    return LombokIconGroup.lombok();
  }

  @Override
  public void update(@Nonnull AnActionEvent e) {
    final Project project = e.getData(Project.KEY);
    final boolean shouldShow =
      e.getData(CommonDataKeys.PSI_FILE) instanceof PsiJavaFile && project != null && LombokLibraryUtil.hasLombokLibrary(project);
    e.getPresentation().setEnabledAndVisible(shouldShow);
  }
}
