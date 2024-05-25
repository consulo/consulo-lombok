package de.plushnikov.intellij.plugin.language;

import consulo.language.file.LanguageFileType;
import consulo.localize.LocalizeValue;
import consulo.lombok.impl.icon.LombokIconGroup;
import consulo.ui.image.Image;
import de.plushnikov.intellij.plugin.LombokBundle;
import jakarta.annotation.Nonnull;

public class LombokConfigFileType extends LanguageFileType {
  public static final LombokConfigFileType INSTANCE = new LombokConfigFileType();

  private LombokConfigFileType() {
    super(LombokConfigLanguage.INSTANCE);
  }

  @Nonnull
  @Override
  public String getId() {
    return "LOMBOK_CONFIG";
  }

  @Nonnull
  @Override
  public LocalizeValue getDescription() {
    return LocalizeValue.localizeTODO(LombokBundle.message("filetype.lombok.config.description"));
  }

  @Nonnull
  @Override
  public String getDefaultExtension() {
    return "config";
  }

  @Override
  public Image getIcon() {
    return LombokIconGroup.config();
  }
}
