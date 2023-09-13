package de.plushnikov.intellij.plugin.language;

import consulo.language.lexer.FlexAdapter;

public class LombokConfigLexerAdapter extends FlexAdapter {
  public LombokConfigLexerAdapter() {
    super(new LombokConfigLexer(null));
  }
}
