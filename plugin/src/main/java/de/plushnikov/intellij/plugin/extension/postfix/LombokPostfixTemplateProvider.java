package de.plushnikov.intellij.plugin.extension.postfix;

import com.intellij.java.impl.codeInsight.completion.JavaCompletionContributor;
import com.intellij.java.language.JavaLanguage;
import consulo.annotation.component.ExtensionImpl;
import consulo.codeEditor.Editor;
import consulo.document.Document;
import consulo.language.Language;
import consulo.language.editor.completion.CompletionInitializationContext;
import consulo.language.editor.postfixTemplate.PostfixTemplate;
import consulo.language.editor.postfixTemplate.PostfixTemplateProvider;
import consulo.language.impl.psi.PsiFileImpl;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiFileFactory;
import consulo.language.template.TemplateLanguageUtil;
import consulo.language.util.LanguageUtil;
import consulo.undoRedo.util.UndoUtil;
import consulo.virtualFileSystem.VirtualFile;
import consulo.virtualFileSystem.fileType.FileType;
import jakarta.annotation.Nonnull;

import java.util.Set;

@ExtensionImpl
public class LombokPostfixTemplateProvider extends PostfixTemplateProvider {

  @Override
  protected Set<PostfixTemplate> buildTemplates() {
    return Set.of(new LombokValPostfixTemplate(), new LombokVarPostfixTemplate());
  }

  @Override
  public boolean isTerminalSymbol(char currentChar) {
    return currentChar == '.' || currentChar == '!';
  }

  @Override
  public void preExpand(@Nonnull PsiFile file, @Nonnull Editor editor) {
  }

  @Override
  public void afterExpand(@Nonnull PsiFile file, @Nonnull Editor editor) {
  }

  @Nonnull
  @Override
  public PsiFile preCheck(@Nonnull PsiFile copyFile, @Nonnull Editor realEditor, int currentOffset) {
    Document document = copyFile.getViewProvider().getDocument();
    assert document != null;
    CharSequence sequence = document.getCharsSequence();
    StringBuilder fileContentWithSemicolon = new StringBuilder(sequence);
    if (isSemicolonNeeded(copyFile, realEditor)) {
      fileContentWithSemicolon.insert(currentOffset, ';');
      // TODO replace return PostfixLiveTemplate.copyFile(copyFile, fileContentWithSemicolon);
      return copyFile(copyFile, fileContentWithSemicolon);
    }

    return copyFile;
  }

  @Nonnull
  public static PsiFile copyFile(@Nonnull PsiFile file, @Nonnull StringBuilder fileContentWithoutKey) {
    PsiFileFactory psiFileFactory = PsiFileFactory.getInstance(file.getProject());
    FileType fileType = file.getFileType();
    Language language = LanguageUtil.getLanguageForPsi(file.getProject(), file.getVirtualFile(), fileType);
    PsiFile copy = language != null ? psiFileFactory.createFileFromText(file.getName(), language, fileContentWithoutKey, false, true)
      : psiFileFactory.createFileFromText(file.getName(), fileType, fileContentWithoutKey);

    if (copy instanceof PsiFileImpl) {
      ((PsiFileImpl)copy).setOriginalFile(TemplateLanguageUtil.getBaseFile(file));
    }

    VirtualFile vFile = copy.getVirtualFile();
    if (vFile != null) {
      UndoUtil.disableUndoFor(vFile);
    }
    return copy;
  }

  private static boolean isSemicolonNeeded(@Nonnull PsiFile file, @Nonnull Editor editor) {
    int startOffset = CompletionInitializationContext.calcStartOffset(editor.getCaretModel().getCurrentCaret());
    return JavaCompletionContributor.semicolonNeeded(file, startOffset);
  }

  @Nonnull
  @Override
  public Language getLanguage() {
    return JavaLanguage.INSTANCE;
  }
}
