package de.plushnikov.intellij.plugin.action.delombok;

import com.intellij.java.language.impl.JavaFileType;
import com.intellij.java.language.psi.PsiAnnotation;
import com.intellij.java.language.psi.PsiClass;
import com.intellij.java.language.psi.PsiJavaFile;
import consulo.annotation.access.RequiredReadAction;
import consulo.codeEditor.Editor;
import consulo.dataContext.DataContext;
import consulo.language.editor.util.PsiUtilBase;
import consulo.language.psi.*;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.localize.LocalizeValue;
import consulo.project.Project;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.ex.action.AnAction;
import consulo.ui.ex.action.AnActionEvent;
import consulo.ui.ex.action.Presentation;
import consulo.ui.ex.action.UpdateInBackground;
import consulo.undoRedo.CommandProcessor;
import consulo.util.collection.ContainerUtil;
import consulo.virtualFileSystem.VirtualFile;
import consulo.virtualFileSystem.util.VirtualFileUtil;
import consulo.virtualFileSystem.util.VirtualFileVisitor;
import de.plushnikov.intellij.plugin.util.LombokLibraryUtil;
import de.plushnikov.intellij.plugin.util.PsiClassUtil;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.Collection;

public abstract class AbstractDelombokAction extends AnAction implements UpdateInBackground {
    private DelombokHandler myHandler;

    protected AbstractDelombokAction(@Nonnull LocalizeValue text, @Nonnull LocalizeValue description) {
        super(text, description);
    }

    protected abstract DelombokHandler createHandler();

    private DelombokHandler getHandler() {
        if (null == myHandler) {
            myHandler = createHandler();
        }
        return myHandler;
    }

    @Override
    @RequiredUIAccess
    public void actionPerformed(@Nonnull AnActionEvent event) {
        Project project = event.getData(Project.KEY);
        if (project == null) {
            return;
        }

        PsiDocumentManager psiDocumentManager = PsiDocumentManager.getInstance(project);
        psiDocumentManager.commitAllDocuments();

        DataContext dataContext = event.getDataContext();
        Editor editor = dataContext.getData(Editor.KEY);

        if (null != editor) {
            PsiFile psiFile = PsiUtilBase.getPsiFileInEditor(editor, project);
            if (null != psiFile) {
                PsiClass targetClass = getTargetClass(editor, psiFile);
                if (null != targetClass) {
                    process(project, psiFile, targetClass);
                }
            }
        }
        else {
            VirtualFile[] files = dataContext.getData(VirtualFile.KEY_OF_ARRAY);
            if (null != files) {
                for (VirtualFile file : files) {
                    if (file.isDirectory()) {
                        processDirectory(project, file);
                    }
                    else {
                        processFile(project, file);
                    }
                }
            }
        }
    }

    private void processDirectory(@Nonnull final Project project, @Nonnull VirtualFile vFile) {
        VirtualFileUtil.visitChildrenRecursively(vFile, new VirtualFileVisitor<Void>() {
            @Override
            @RequiredReadAction
            public boolean visitFile(@Nonnull VirtualFile file) {
                if (!file.isDirectory()) {
                    processFile(project, file);
                }
                return true;
            }
        });
    }

    @RequiredReadAction
    private void processFile(Project project, VirtualFile file) {
        if (JavaFileType.INSTANCE.equals(file.getFileType())) {
            PsiManager psiManager = PsiManager.getInstance(project);
            PsiJavaFile psiFile = (PsiJavaFile) psiManager.findFile(file);
            if (psiFile != null) {
                process(project, psiFile);
            }
        }
    }

    protected void process(@Nonnull Project project, @Nonnull PsiJavaFile psiJavaFile) {
        executeCommand(project, () -> getHandler().invoke(project, psiJavaFile));
    }

    protected void process(@Nonnull Project project, @Nonnull PsiFile psiFile, @Nonnull PsiClass psiClass) {
        executeCommand(project, () -> getHandler().invoke(project, psiFile, psiClass));
    }

    private void executeCommand(Project project, Runnable action) {
        CommandProcessor.getInstance().newCommand()
            .project(project)
            .name(getTemplatePresentation().getDescriptionValue())
            .inWriteAction()
            .run(action);
    }

    @Override
    public void update(@Nonnull AnActionEvent event) {
        Presentation presentation = event.getPresentation();
        DataContext dataContext = event.getDataContext();

        Project project = event.getData(Project.KEY);
        if (project == null || !LombokLibraryUtil.hasLombokLibrary(project)) {
            presentation.setEnabled(false);
            return;
        }

        Editor editor = dataContext.getData(Editor.KEY);
        if (null != editor) {
            PsiFile file = PsiUtilBase.getPsiFileInEditor(editor, project);
            presentation.setEnabled(file != null && isValidForFile(editor, file));
            return;
        }

        boolean isValid = false;
        VirtualFile[] files = dataContext.getData(VirtualFile.KEY_OF_ARRAY);
        if (null != files) {
            PsiManager psiManager = PsiManager.getInstance(project);
            for (VirtualFile file : files) {
                if (file.isDirectory()) {
                    //directory is valid
                    isValid = true;
                    break;
                }
                if (JavaFileType.INSTANCE.equals(file.getFileType())) {
                    PsiJavaFile psiFile = (PsiJavaFile) psiManager.findFile(file);
                    if (psiFile != null) {
                        isValid = ContainerUtil.or(psiFile.getClasses(), this::isValidForClass);
                    }
                }
                if (isValid) {
                    break;
                }
            }
        }
        presentation.setEnabled(isValid);
    }

    private boolean isValidForClass(@Nonnull PsiClass psiClass) {
        if (psiClass.isInterface()) {
            return false;
        }
        Collection<PsiAnnotation> psiAnnotations = getHandler().collectProcessableAnnotations(psiClass);
        if (!psiAnnotations.isEmpty()) {
            return true;
        }
        Collection<PsiClass> classesIntern = PsiClassUtil.collectInnerClassesIntern(psiClass);
        return ContainerUtil.exists(classesIntern, this::isValidForClass);
    }

    @Nullable
    @RequiredReadAction
    private static PsiClass getTargetClass(Editor editor, PsiFile file) {
        int offset = editor.getCaretModel().getOffset();
        PsiElement element = file.findElementAt(offset);
        if (element == null) {
            return null;
        }
        PsiClass target = PsiTreeUtil.getParentOfType(element, PsiClass.class);
        return target instanceof SyntheticElement ? null : target;
    }

    @RequiredReadAction
    private boolean isValidForFile(@Nonnull Editor editor, @Nonnull PsiFile file) {
        if (!(file instanceof PsiJavaFile)) {
            return false;
        }
        if (file instanceof PsiCompiledElement) {
            return false;
        }
        if (!file.isWritable()) {
            return false;
        }

        PsiClass targetClass = getTargetClass(editor, file);
        return targetClass != null && isValidForClass(targetClass);
    }
}
