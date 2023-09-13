package de.plushnikov.intellij.plugin.settings;

import consulo.project.Project;
import consulo.project.ProjectPropertiesComponent;
import org.jetbrains.annotations.NotNull;

public final class ProjectSettings {
  private static final String PREFIX = "LombokPlugin";

  public static final String IS_LOMBOK_VERSION_CHECK_ENABLED = PREFIX + "IS_LOMBOK_VERSION_CHECK_Enabled";
  public static final String IS_LOMBOK_JPS_FIX_ENABLED = PREFIX + "IS_LOMBOK_JPS_FIX_ENABLED";

  public static boolean isEnabled(@NotNull Project project, final String propertyName) {
    return ProjectPropertiesComponent.getInstance(project).getBoolean(propertyName, true);
  }

  public static boolean isEnabled(@NotNull Project project, final String propertyName, boolean defaultValue) {
    return ProjectPropertiesComponent.getInstance(project).getBoolean(propertyName, defaultValue);
  }

  public static void setEnabled(@NotNull Project project, final String propertyName, boolean value) {
    ProjectPropertiesComponent.getInstance(project).setValue(propertyName, String.valueOf(value), "");
  }
}
