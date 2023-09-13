package de.plushnikov.intellij.plugin.language.psi;

import consulo.language.file.FileViewProvider;
import consulo.language.impl.psi.PsiFileBase;
import de.plushnikov.intellij.plugin.language.LombokConfigLanguage;
import org.jetbrains.annotations.NotNull;

public class LombokConfigFile extends PsiFileBase {
  public LombokConfigFile(@NotNull FileViewProvider viewProvider) {
    super(viewProvider, LombokConfigLanguage.INSTANCE);
  }

  @Override
  public String toString() {
    return "LombokConfigFile";
  }
}
