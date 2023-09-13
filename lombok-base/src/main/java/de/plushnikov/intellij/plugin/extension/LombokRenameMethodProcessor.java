package de.plushnikov.intellij.plugin.extension;

import com.intellij.java.language.psi.PsiAnnotation;
import consulo.codeEditor.Editor;
import consulo.language.editor.refactoring.rename.RenamePsiElementProcessor;
import consulo.language.psi.PsiElement;
import de.plushnikov.intellij.plugin.psi.LombokLightFieldBuilder;
import de.plushnikov.intellij.plugin.psi.LombokLightMethodBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * RenameProcessor for replacement of lombok virtual methods/fields with root elements
 */
public class LombokRenameMethodProcessor extends RenamePsiElementProcessor {

  @Override
  public boolean canProcessElement(@NotNull PsiElement elem) {
    return (elem instanceof LombokLightMethodBuilder || elem instanceof LombokLightFieldBuilder)
      && !(elem.getNavigationElement() instanceof PsiAnnotation);
  }

  @Override
  @Nullable
  public PsiElement substituteElementToRename(@NotNull PsiElement elem, @Nullable Editor editor) {
    return elem.getNavigationElement();
  }
}
