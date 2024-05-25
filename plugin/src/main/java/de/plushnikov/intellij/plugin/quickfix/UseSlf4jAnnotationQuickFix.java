package de.plushnikov.intellij.plugin.quickfix;

import com.intellij.java.analysis.impl.codeInsight.intention.AddAnnotationFix;
import com.intellij.java.language.psi.PsiClass;
import com.intellij.java.language.psi.PsiJavaFile;
import com.intellij.java.language.psi.codeStyle.JavaCodeStyleManager;
import consulo.language.editor.intention.IntentionAction;
import consulo.language.psi.*;
import consulo.language.psi.search.ReferencesSearch;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.project.Project;
import de.plushnikov.intellij.plugin.LombokClassNames;
import jakarta.annotation.Nonnull;

import java.util.Collection;

import static de.plushnikov.intellij.plugin.processor.clazz.log.AbstractLogProcessor.getLoggerName;

public class UseSlf4jAnnotationQuickFix extends AddAnnotationFix implements IntentionAction {

  @Nonnull
  private final SmartPsiElementPointer<PsiNamedElement> elementToRemove;

  public UseSlf4jAnnotationQuickFix(@Nonnull PsiNamedElement elementToRemove, @Nonnull PsiClass containingClass) {
    super(LombokClassNames.SLF_4_J, containingClass);
    this.elementToRemove = SmartPointerManager.getInstance(elementToRemove.getProject()).createSmartPsiElementPointer(elementToRemove);
  }

  @Override
  public void invoke(@Nonnull Project project, @Nonnull PsiFile file, @Nonnull PsiElement startElement,
                     @Nonnull PsiElement endElement) {
    super.invoke(project, file, startElement, endElement);

    final PsiNamedElement psiNamedElement = elementToRemove.getElement();
    if (null != psiNamedElement) {
      final Collection<PsiReference> all = ReferencesSearch.search(psiNamedElement).findAll();

      final String loggerName = getLoggerName(PsiTreeUtil.getParentOfType(psiNamedElement, PsiClass.class));
      for (PsiReference psiReference : all) {
        psiReference.handleElementRename(loggerName);
      }

      psiNamedElement.delete();

      JavaCodeStyleManager.getInstance(project).removeRedundantImports((PsiJavaFile) file);
    }
  }
}
