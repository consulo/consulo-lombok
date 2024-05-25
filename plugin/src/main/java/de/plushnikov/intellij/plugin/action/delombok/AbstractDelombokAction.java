package de.plushnikov.intellij.plugin.action.delombok;

import com.intellij.java.language.impl.JavaFileType;
import com.intellij.java.language.psi.PsiAnnotation;
import com.intellij.java.language.psi.PsiClass;
import com.intellij.java.language.psi.PsiJavaFile;
import consulo.application.ApplicationManager;
import consulo.codeEditor.Editor;
import consulo.dataContext.DataContext;
import consulo.language.editor.CommonDataKeys;
import consulo.language.editor.util.PsiUtilBase;
import consulo.language.psi.*;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.project.Project;
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

  protected AbstractDelombokAction() {
    //default constructor
  }

//  @Override
//  public @NotNull ActionUpdateThread getActionUpdateThread() {
//    return ActionUpdateThread.BGT;
//  }

  protected abstract DelombokHandler createHandler();

  private DelombokHandler getHandler() {
    if (null == myHandler) {
      myHandler = createHandler();
    }
    return myHandler;
  }

  @Override
  public void actionPerformed(@Nonnull AnActionEvent event) {
    final Project project = event.getData(Project.KEY);
    if (project == null) {
      return;
    }

    final PsiDocumentManager psiDocumentManager = PsiDocumentManager.getInstance(project);
    psiDocumentManager.commitAllDocuments();

    final DataContext dataContext = event.getDataContext();
    final Editor editor = dataContext.getData(CommonDataKeys.EDITOR);

    if (null != editor) {
      final PsiFile psiFile = PsiUtilBase.getPsiFileInEditor(editor, project);
      if (null != psiFile) {
        final PsiClass targetClass = getTargetClass(editor, psiFile);
        if (null != targetClass) {
          process(project, psiFile, targetClass);
        }
      }
    } else {
      final VirtualFile[] files = dataContext.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY);
      if (null != files) {
        for (VirtualFile file : files) {
          if (file.isDirectory()) {
            processDirectory(project, file);
          } else {
            processFile(project, file);
          }
        }
      }
    }
  }

  private void processDirectory(@Nonnull final Project project, @Nonnull VirtualFile vFile) {
    VirtualFileUtil.visitChildrenRecursively(vFile, new VirtualFileVisitor<Void>() {
      @Override
      public boolean visitFile(@Nonnull VirtualFile file) {
        if (!file.isDirectory()) {
          processFile(project, file);
        }
        return true;
      }
    });
  }

  private void processFile(Project project, VirtualFile file) {
    if (JavaFileType.INSTANCE.equals(file.getFileType())) {
      final PsiManager psiManager = PsiManager.getInstance(project);
      PsiJavaFile psiFile = (PsiJavaFile) psiManager.findFile(file);
      if (psiFile != null) {
        process(project, psiFile);
      }
    }
  }

  protected void process(@Nonnull final Project project, @Nonnull final PsiJavaFile psiJavaFile) {
    executeCommand(project, () -> getHandler().invoke(project, psiJavaFile));
  }

  protected void process(@Nonnull final Project project, @Nonnull final PsiFile psiFile, @Nonnull final PsiClass psiClass) {
    executeCommand(project, () -> getHandler().invoke(project, psiFile, psiClass));
  }

  private void executeCommand(final Project project, final Runnable action) {
    CommandProcessor.getInstance().executeCommand(project,
                                                  () -> ApplicationManager.getApplication().runWriteAction(action), getCommandName(), null);
  }

  @Override
  public void update(@Nonnull AnActionEvent event) {
    final Presentation presentation = event.getPresentation();
    final DataContext dataContext = event.getDataContext();

    final Project project = event.getData(Project.KEY);
    if (project == null || !LombokLibraryUtil.hasLombokLibrary(project)) {
      presentation.setEnabled(false);
      return;
    }

    final Editor editor = dataContext.getData(CommonDataKeys.EDITOR);
    if (null != editor) {
      final PsiFile file = PsiUtilBase.getPsiFileInEditor(editor, project);
      presentation.setEnabled(file != null && isValidForFile(editor, file));
      return;
    }

    boolean isValid = false;
    final VirtualFile[] files = dataContext.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY);
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
    final Collection<PsiClass> classesIntern = PsiClassUtil.collectInnerClassesIntern(psiClass);
    return ContainerUtil.exists(classesIntern, this::isValidForClass);
  }

  @Nullable
  private static PsiClass getTargetClass(Editor editor, PsiFile file) {
    int offset = editor.getCaretModel().getOffset();
    PsiElement element = file.findElementAt(offset);
    if (element == null) {
      return null;
    }
    final PsiClass target = PsiTreeUtil.getParentOfType(element, PsiClass.class);
    return target instanceof SyntheticElement ? null : target;
  }

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

  private String getCommandName() {
    String text = getTemplatePresentation().getText();
    return text == null ? "" : text;
  }
}
