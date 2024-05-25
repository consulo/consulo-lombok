package de.plushnikov.intellij.plugin.action.inline;

import com.intellij.java.impl.refactoring.inline.InlineMethodHandler;
import com.intellij.java.impl.refactoring.inline.JavaInlineActionHandler;
import com.intellij.java.language.JavaLanguage;
import consulo.annotation.component.ExtensionImpl;
import consulo.codeEditor.Editor;
import consulo.language.psi.PsiElement;
import consulo.project.Project;
import de.plushnikov.intellij.plugin.psi.LombokLightMethodBuilder;

/**
 * Custom InlineMethodHandler to support lombok generated methods
 */
@ExtensionImpl
public class LombokInlineMethodHandler extends JavaInlineActionHandler {

  @Override
  public boolean canInlineElement(PsiElement element) {
    return element instanceof LombokLightMethodBuilder && element.getLanguage() == JavaLanguage.INSTANCE;
  }

  @Override
  public void inlineElement(final Project project, Editor editor, PsiElement element) {
    InlineMethodHandler handler = new InlineMethodHandler();

    handler.inlineElement(project, editor, element);
  }
}
