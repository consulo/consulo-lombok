package de.plushnikov.intellij.plugin;

import consulo.annotation.internal.MigratedExtensionsTo;
import consulo.component.util.localize.AbstractBundle;
import consulo.lombok.localize.LombokLocalize;
import jakarta.annotation.Nonnull;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.PropertyKey;

import java.util.ResourceBundle;

/**
 * {@link ResourceBundle}/localization utils for the lombok plugin.
 */
@Deprecated
@MigratedExtensionsTo(LombokLocalize.class)
public final class LombokBundle extends AbstractBundle {
  @NonNls
  public static final String PATH_TO_BUNDLE = "messages.LombokBundle";
  private static final LombokBundle ourInstance = new LombokBundle();

  private LombokBundle() {
    super(PATH_TO_BUNDLE);
  }

  public static @Nls String message(@Nonnull @PropertyKey(resourceBundle = PATH_TO_BUNDLE) String key, @Nonnull Object... params) {
    return ourInstance.getMessage(key, params);
  }
}
