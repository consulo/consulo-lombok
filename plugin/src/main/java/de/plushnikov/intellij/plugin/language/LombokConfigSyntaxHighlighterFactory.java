package de.plushnikov.intellij.plugin.language;

import consulo.annotation.component.ExtensionImpl;
import consulo.language.Language;
import consulo.language.editor.highlight.SyntaxHighlighter;
import consulo.language.editor.highlight.SyntaxHighlighterFactory;
import consulo.project.Project;
import consulo.virtualFileSystem.VirtualFile;
import jakarta.annotation.Nonnull;

@ExtensionImpl
public class LombokConfigSyntaxHighlighterFactory extends SyntaxHighlighterFactory {
  @Nonnull
  @Override
  public SyntaxHighlighter getSyntaxHighlighter(Project project, VirtualFile virtualFile) {
    return new LombokConfigSyntaxHighlighter();
  }

  @Nonnull
  @Override
  public Language getLanguage() {
    return LombokConfigLanguage.INSTANCE;
  }
}
