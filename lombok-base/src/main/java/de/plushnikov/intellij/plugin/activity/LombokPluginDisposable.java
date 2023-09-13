package de.plushnikov.intellij.plugin.activity;

import consulo.annotation.component.ComponentScope;
import consulo.annotation.component.ServiceAPI;
import consulo.annotation.component.ServiceImpl;
import consulo.disposer.Disposable;
import consulo.project.Project;
import jakarta.inject.Singleton;
import org.jetbrains.annotations.NotNull;

@ServiceAPI(ComponentScope.PROJECT)
@ServiceImpl
@Singleton
public final class LombokPluginDisposable implements Disposable {

  public static Disposable getInstance(@NotNull Project project) {
    return project.getInstance(LombokPluginDisposable.class);
  }

  @Override
  public void dispose() {
  }
}
