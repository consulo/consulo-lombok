package de.plushnikov.intellij.plugin.language;

import consulo.annotation.component.ExtensionImpl;
import consulo.language.Language;
import consulo.language.ast.ASTNode;
import consulo.language.ast.IFileElementType;
import consulo.language.ast.TokenSet;
import consulo.language.file.FileViewProvider;
import consulo.language.lexer.Lexer;
import consulo.language.parser.ParserDefinition;
import consulo.language.parser.PsiParser;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.version.LanguageVersion;
import de.plushnikov.intellij.plugin.language.parser.LombokConfigParser;
import de.plushnikov.intellij.plugin.language.psi.LombokConfigFile;
import de.plushnikov.intellij.plugin.language.psi.LombokConfigTypes;
import jakarta.annotation.Nonnull;
import org.jetbrains.annotations.NotNull;

@ExtensionImpl
public class LombokConfigParserDefinition implements ParserDefinition {

  private static class LombokConfigParserTokenSets {
    private static final TokenSet COMMENTS = TokenSet.create(LombokConfigTypes.COMMENT);
  }

  private static final IFileElementType FILE = new IFileElementType(Language.findInstance(LombokConfigLanguage.class));

  @NotNull
  @Override
  public Lexer createLexer(LanguageVersion languageVersion) {
    return new LombokConfigLexerAdapter();
  }

  @Override
  @NotNull
  public TokenSet getCommentTokens(LanguageVersion languageVersion) {
    return LombokConfigParserTokenSets.COMMENTS;
  }

  @Override
  @NotNull
  public TokenSet getStringLiteralElements(LanguageVersion languageVersion) {
    return TokenSet.EMPTY;
  }

  @Override
  @NotNull
  public PsiParser createParser(final LanguageVersion languageVersion) {
    return new LombokConfigParser();
  }

  @Override
  public @NotNull IFileElementType getFileNodeType() {
    return FILE;
  }

  @Override
  public @NotNull PsiFile createFile(@NotNull FileViewProvider viewProvider) {
    return new LombokConfigFile(viewProvider);
  }

  @Override
  @NotNull
  public PsiElement createElement(ASTNode node) {
    return LombokConfigTypes.Factory.createElement(node);
  }

  @Nonnull
  @Override
  public Language getLanguage() {
    return LombokConfigLanguage.INSTANCE;
  }
}
