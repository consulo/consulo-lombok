package de.plushnikov.intellij.plugin.language;

import consulo.language.file.LanguageFileType;
import consulo.lombok.impl.icon.LombokIconGroup;
import consulo.ui.image.Image;
import de.plushnikov.intellij.plugin.LombokBundle;
import org.jetbrains.annotations.NotNull;

public class LombokConfigFileType extends LanguageFileType {
  public static final LombokConfigFileType INSTANCE = new LombokConfigFileType();

  private LombokConfigFileType() {
    super(LombokConfigLanguage.INSTANCE);
  }

  @NotNull
  @Override
  public String getId() {
    return "LOMBOK_CONFIG";
  }

  @NotNull
  @Override
  public String getDescription() {
    return LombokBundle.message("filetype.lombok.config.description");
  }

  @NotNull
  @Override
  public String getDefaultExtension() {
    return "config";
  }

  @Override
  public Image getIcon() {
    return LombokIconGroup.config();
  }
}
