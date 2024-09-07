package de.plushnikov.intellij.plugin.extension;

import com.intellij.java.language.psi.PsiAnnotation;
import consulo.annotation.component.ExtensionImpl;
import consulo.codeEditor.Editor;
import consulo.dataContext.DataContext;
import consulo.language.editor.refactoring.RefactoringBundle;
import consulo.language.editor.refactoring.rename.PsiElementRenameHandler;
import consulo.language.editor.refactoring.rename.RenameHandler;
import consulo.language.editor.refactoring.util.CommonRefactoringUtil;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.localize.LocalizeValue;
import consulo.project.Project;
import de.plushnikov.intellij.plugin.LombokBundle;
import de.plushnikov.intellij.plugin.psi.LombokLightClassBuilder;
import de.plushnikov.intellij.plugin.psi.LombokLightFieldBuilder;
import de.plushnikov.intellij.plugin.psi.LombokLightMethodBuilder;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * "Rename"-Handler Vetoer to disable renaming not supported lombok generated  methods
 */
@ExtensionImpl(order = "before member")
public class LombokElementRenameVetoHandler implements RenameHandler {
  @Override
  public boolean isAvailableOnDataContext(DataContext dataContext) {
    final PsiElement element = PsiElementRenameHandler.getElement(dataContext);
    return element instanceof LombokLightClassBuilder ||
      ((element instanceof LombokLightMethodBuilder || element instanceof LombokLightFieldBuilder)
        && element.getNavigationElement() instanceof PsiAnnotation);
  }

  @Override
  public boolean isRenaming(DataContext dataContext) {
    return isAvailableOnDataContext(dataContext);
  }

  @Nonnull
  @Override
  public LocalizeValue getActionTitleValue() {
    return LocalizeValue.localizeTODO("Lombok Veto Renamer");
  }

  @Override
  public void invoke(@Nonnull Project project, Editor editor, PsiFile file, @Nullable DataContext dataContext) {
    invokeInner(project, editor);
  }

  @Override
  public void invoke(@Nonnull Project project, @Nonnull PsiElement[] elements, @Nullable DataContext dataContext) {
    Editor editor = dataContext == null ? null : dataContext.getData(Editor.KEY);
    invokeInner(project, editor);
  }

  private static void invokeInner(Project project, Editor editor) {
    CommonRefactoringUtil.showErrorHint(project, editor,
                                        RefactoringBundle.getCannotRefactorMessage(LombokBundle.message(
                                          "dialog.message.this.element.cannot.be.renamed")),
                                        RefactoringBundle.message("rename.title"), null);
  }
}
