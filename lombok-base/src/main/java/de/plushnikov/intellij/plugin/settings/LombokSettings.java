package de.plushnikov.intellij.plugin.settings;

import consulo.application.ApplicationManager;
import consulo.component.persist.PersistentStateComponent;
import consulo.component.persist.State;
import consulo.component.persist.Storage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Persistent global settings object for the Lombok plugin.
 */
@State(
  name = "LombokSettings",
  storages = @Storage("lombok-plugin.xml")
)
public class LombokSettings implements PersistentStateComponent<LombokPluginState> {

  /**
   * Get the instance of this service.
   *
   * @return the unique {@link LombokSettings} instance.
   */
  public static LombokSettings getInstance() {
    return ApplicationManager.getApplication().getInstance(LombokSettings.class);
  }

  private LombokPluginState myState = new LombokPluginState();

  @Nullable
  @Override
  public LombokPluginState getState() {
    return myState;
  }

  @Override
  public void loadState(@NotNull LombokPluginState element) {
    myState = element;
  }

  public String getVersion() {
    return myState.getPluginVersion();
  }

  public void setVersion(String version) {
    myState.setPluginVersion(version);
  }

}
