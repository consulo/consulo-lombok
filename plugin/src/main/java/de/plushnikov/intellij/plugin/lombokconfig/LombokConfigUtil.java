package de.plushnikov.intellij.plugin.lombokconfig;

import consulo.language.psi.util.PsiTreeUtil;
import de.plushnikov.intellij.plugin.language.psi.LombokConfigCleaner;
import de.plushnikov.intellij.plugin.language.psi.LombokConfigFile;
import de.plushnikov.intellij.plugin.language.psi.LombokConfigProperty;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public final class LombokConfigUtil {

  private static final LombokConfigProperty[] EMPTY_LOMBOK_CONFIG_PROPERTIES = new LombokConfigProperty[0];
  private static final LombokConfigCleaner[] EMPTY_LOMBOK_CONFIG_CLEANERS = new LombokConfigCleaner[0];

  @Nonnull
  public static LombokConfigProperty[] getLombokConfigProperties(@Nullable LombokConfigFile lombokConfigFile) {
    LombokConfigProperty[] result = PsiTreeUtil.getChildrenOfType(lombokConfigFile, LombokConfigProperty.class);
    return null == result ? EMPTY_LOMBOK_CONFIG_PROPERTIES : result;
  }

  @Nonnull
  public static LombokConfigCleaner[] getLombokConfigCleaners(@Nullable LombokConfigFile lombokConfigFile) {
    LombokConfigCleaner[] result = PsiTreeUtil.getChildrenOfType(lombokConfigFile, LombokConfigCleaner.class);
    return null == result ? EMPTY_LOMBOK_CONFIG_CLEANERS : result;
  }
}
