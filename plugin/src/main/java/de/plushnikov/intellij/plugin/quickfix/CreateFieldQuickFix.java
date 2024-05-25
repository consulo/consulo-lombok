package de.plushnikov.intellij.plugin.quickfix;

import com.intellij.java.impl.codeInsight.CodeInsightUtil;
import com.intellij.java.impl.codeInsight.generation.GenerateMembersUtil;
import com.intellij.java.impl.codeInsight.generation.PsiGenerationInfo;
import com.intellij.java.language.psi.*;
import consulo.codeEditor.Editor;
import consulo.language.editor.WriteCommandAction;
import consulo.language.editor.inspection.LocalQuickFixOnPsiElement;
import consulo.language.editor.util.LanguageUndoUtil;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.project.Project;
import de.plushnikov.intellij.plugin.LombokBundle;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author Plushnikov Michail
 */
public class CreateFieldQuickFix extends LocalQuickFixOnPsiElement {
  private final String myName;
  private final PsiType myType;
  private final String myInitializerText;
  private final Collection<String> myModifiers;

  public CreateFieldQuickFix(@Nonnull PsiClass psiClass, @Nonnull String name, @Nonnull PsiType psiType, @Nullable String initializerText, String... modifiers) {
    super(psiClass);
    myName = name;
    myType = psiType;
    myInitializerText = initializerText;
    myModifiers = Arrays.asList(modifiers);
  }

  @Override
  @Nonnull
  public String getText() {
    return LombokBundle.message("intention.name.create.new.field.s", myName);
  }

  @Override
  @Nonnull
  public String getFamilyName() {
    return getText();
  }

  @Override
  public void invoke(@Nonnull Project project, @Nonnull PsiFile psiFile, @Nonnull PsiElement startElement, @Nonnull PsiElement endElement) {
    final PsiClass myClass = (PsiClass) startElement;
    final Editor editor = CodeInsightUtil.positionCursor(project, psiFile, myClass.getLBrace());
    if (editor != null) {
      WriteCommandAction.writeCommandAction(psiFile).run(() ->
        {
          final PsiElementFactory psiElementFactory = JavaPsiFacade.getElementFactory(project);
          final PsiField psiField = psiElementFactory.createField(myName, myType);

          final PsiModifierList modifierList = psiField.getModifierList();
          if (null != modifierList) {
            for (String modifier : myModifiers) {
              modifierList.setModifierProperty(modifier, true);
            }
          }
          if (null != myInitializerText) {
            PsiExpression psiInitializer = psiElementFactory.createExpressionFromText(myInitializerText, psiField);
            psiField.setInitializer(psiInitializer);
          }

          final List<PsiGenerationInfo<PsiField>> generationInfos = GenerateMembersUtil.insertMembersAtOffset(myClass.getContainingFile(), editor.getCaretModel().getOffset(),
                                                                                                              Collections.singletonList(new PsiGenerationInfo<>(psiField)));
          if (!generationInfos.isEmpty()) {
            PsiField psiMember = generationInfos.iterator().next().getPsiMember();
            editor.getCaretModel().moveToOffset(psiMember.getTextRange().getEndOffset());
          }

          LanguageUndoUtil.markPsiFileForUndo(psiFile);
        }
      );
    }
  }

  @Override
  public boolean startInWriteAction() {
    return false;
  }

}
