package de.plushnikov.intellij.plugin;

import com.intellij.DynamicBundle;
import consulo.component.util.localize.AbstractBundle;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.PropertyKey;

import java.util.ResourceBundle;
import java.util.function.Supplier;

/**
 * {@link ResourceBundle}/localization utils for the lombok plugin.
 */
public final class LombokBundle extends AbstractBundle {
  @NonNls
  public static final String PATH_TO_BUNDLE = "messages.LombokBundle";
  private static final LombokBundle ourInstance = new LombokBundle();

  private LombokBundle() {
    super(PATH_TO_BUNDLE);
  }

  public static @Nls String message(@NotNull @PropertyKey(resourceBundle = PATH_TO_BUNDLE) String key, Object @NotNull ... params) {
    return ourInstance.getMessage(key, params);
  }
}
