package de.plushnikov.intellij.plugin.settings;

import consulo.annotation.component.ComponentScope;
import consulo.annotation.component.ServiceAPI;
import consulo.annotation.component.ServiceImpl;
import consulo.application.ApplicationManager;
import consulo.component.persist.PersistentStateComponent;
import consulo.component.persist.State;
import consulo.component.persist.Storage;
import jakarta.inject.Singleton;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * Persistent global settings object for the Lombok plugin.
 */
@Singleton
@State(
  name = "LombokSettings",
  storages = @Storage("lombok-plugin.xml")
)
@ServiceAPI(ComponentScope.APPLICATION)
@ServiceImpl
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
  public void loadState(@Nonnull LombokPluginState element) {
    myState = element;
  }

  public String getVersion() {
    return myState.getPluginVersion();
  }

  public void setVersion(String version) {
    myState.setPluginVersion(version);
  }

}
