package de.plushnikov.intellij.plugin.language.psi;

import consulo.language.ast.IElementType;
import de.plushnikov.intellij.plugin.language.LombokConfigLanguage;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class LombokConfigTokenType extends IElementType {
  public LombokConfigTokenType(@NotNull @NonNls String debugName) {
    super(debugName, LombokConfigLanguage.INSTANCE);
  }

  @Override
  public String toString() {
    return "LombokConfigTokenType." + super.toString();
  }
}
