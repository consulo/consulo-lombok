package de.plushnikov.intellij.plugin.activity;

import com.intellij.java.compiler.impl.javaCompiler.annotationProcessing.AnnotationProcessingConfiguration;
import consulo.application.ReadAction;
import consulo.compiler.CompilerConfiguration;
import consulo.project.Project;
import consulo.project.ui.notification.NotificationAction;
import consulo.project.ui.wm.StatusBar;
import consulo.project.ui.wm.WindowManager;
import consulo.ui.ex.RelativePoint;
import consulo.ui.ex.action.AnActionEvent;
import consulo.ui.ex.popup.Balloon;
import consulo.ui.ex.popup.JBPopupFactory;
import consulo.util.collection.ContainerUtil;
import de.plushnikov.intellij.plugin.LombokBundle;
import de.plushnikov.intellij.plugin.Version;
import de.plushnikov.intellij.plugin.util.LombokLibraryUtil;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class LombokBuildManagerListener implements BuildManagerListener {
  private final SingletonNotificationManager myNotificationManager = new SingletonNotificationManager(Version.PLUGIN_NAME, NotificationType.ERROR);

  @Override
  public void beforeBuildProcessStarted(@NotNull Project project,
                                        @NotNull UUID sessionId) {
    if (!hasAnnotationProcessorsEnabled(project) &&
        ReadAction.nonBlocking(() -> LombokLibraryUtil.hasLombokLibrary(project)).executeSynchronously()) {
      suggestEnableAnnotations(project);
    }
  }

  private static CompilerConfigurationImpl getCompilerConfiguration(@NotNull Project project) {
    return (CompilerConfigurationImpl)CompilerConfiguration.getInstance(project);
  }

  private static boolean hasAnnotationProcessorsEnabled(@NotNull Project project) {
    final CompilerConfigurationImpl compilerConfiguration = getCompilerConfiguration(project);
    return compilerConfiguration.getDefaultProcessorProfile().isEnabled() &&
           ContainerUtil.and(compilerConfiguration.getModuleProcessorProfiles(), AnnotationProcessingConfiguration::isEnabled);
  }

  private static void enableAnnotationProcessors(@NotNull Project project) {
    CompilerConfigurationImpl compilerConfiguration = getCompilerConfiguration(project);
    compilerConfiguration.getDefaultProcessorProfile().setEnabled(true);
    compilerConfiguration.getModuleProcessorProfiles().forEach(pp -> pp.setEnabled(true));

    StatusBar statusBar = WindowManager.getInstance().getStatusBar(project);
    JBPopupFactory.getInstance()
                  .createHtmlTextBalloonBuilder(
        LombokBundle.message("popup.content.java.annotation.processing.has.been.enabled"),
        MessageType.INFO,
        null
      )
                  .setFadeoutTime(3000)
                  .createBalloon()
                  .show(RelativePoint.getNorthEastOf(statusBar.getComponent()), Balloon.Position.atRight);
  }

  private void suggestEnableAnnotations(Project project) {
    myNotificationManager.notify("", LombokBundle.message("config.warn.annotation-processing.disabled.title"), project, (notification) -> {
      notification.setSuggestionType(true);
      notification.addAction(new NotificationAction(LombokBundle.message("notification.enable.annotation.processing")) {
        @Override
        public void actionPerformed(@NotNull AnActionEvent e, @NotNull Notification notification) {
          enableAnnotationProcessors(project);
          notification.expire();
        }
      });
    });
  }
}
