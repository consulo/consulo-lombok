package de.plushnikov.intellij.plugin.action;

import com.intellij.java.language.psi.PsiJavaFile;
import consulo.application.dumb.DumbAware;
import consulo.language.psi.PsiFile;
import consulo.localize.LocalizeValue;
import consulo.lombok.impl.icon.LombokIconGroup;
import consulo.project.Project;
import consulo.ui.ex.action.AnActionEvent;
import consulo.ui.ex.action.AnActionWithAsyncUpdate;
import consulo.ui.ex.action.DefaultActionGroup;
import consulo.ui.ex.action.coroutine.ActionSafeReadLock;
import consulo.ui.image.Image;
import consulo.util.concurrent.coroutine.Coroutine;
import de.plushnikov.intellij.plugin.util.LombokLibraryUtil;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class LombokMenuGroup extends DefaultActionGroup implements DumbAware, AnActionWithAsyncUpdate {
    public LombokMenuGroup(@Nonnull LocalizeValue text, @Nonnull LocalizeValue description, @Nullable Image icon) {
        super(text, description, icon);
    }

    @Override
    public Coroutine<?, ?> updateAsync(AnActionEvent e) {
        return ActionSafeReadLock.run(e, presentation -> {
            Project project = e.getData(Project.KEY);
            boolean shouldShow =
                e.getData(PsiFile.KEY) instanceof PsiJavaFile && project != null && LombokLibraryUtil.hasLombokLibrary(project);
            presentation.setEnabledAndVisible(shouldShow);
        }).toCoroutine();
    }
}
