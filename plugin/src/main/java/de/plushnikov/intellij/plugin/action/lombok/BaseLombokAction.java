package de.plushnikov.intellij.plugin.action.lombok;

import com.intellij.java.impl.codeInsight.generation.actions.BaseGenerateAction;
import consulo.annotation.access.RequiredReadAction;
import consulo.codeEditor.Editor;
import consulo.language.editor.action.CodeInsightActionHandler;
import consulo.language.psi.PsiFile;
import consulo.localize.LocalizeValue;
import consulo.project.Project;
import de.plushnikov.intellij.plugin.util.LombokLibraryUtil;
import jakarta.annotation.Nonnull;

public abstract class BaseLombokAction extends BaseGenerateAction {
    protected BaseLombokAction(CodeInsightActionHandler handler, @Nonnull LocalizeValue text, @Nonnull LocalizeValue description) {
        super(handler, text);
        getTemplatePresentation().setDescriptionValue(description);
    }

    @Override
    @RequiredReadAction
    protected boolean isValidForFile(@Nonnull Project project, @Nonnull Editor editor, @Nonnull PsiFile file) {
        return file.isWritable() && super.isValidForFile(project, editor, file)
            && LombokLibraryUtil.hasLombokLibrary(project);
    }
}
