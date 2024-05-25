package de.plushnikov.intellij.plugin.language.psi;

import consulo.language.ast.IElementType;
import de.plushnikov.intellij.plugin.language.LombokConfigLanguage;
import org.jetbrains.annotations.NonNls;
import jakarta.annotation.Nonnull;

public class LombokConfigElementType extends IElementType {
  public LombokConfigElementType(@Nonnull @NonNls String debugName) {
    super(debugName, LombokConfigLanguage.INSTANCE);
  }
}
