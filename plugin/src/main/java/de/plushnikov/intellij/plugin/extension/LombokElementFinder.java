package de.plushnikov.intellij.plugin.extension;

import com.intellij.java.language.impl.psi.impl.file.impl.JavaFileManager;
import com.intellij.java.language.psi.PsiClass;
import com.intellij.java.language.psi.PsiElementFinder;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.project.Project;
import de.plushnikov.intellij.plugin.util.LombokLibraryUtil;
import jakarta.annotation.Nonnull;
import jakarta.inject.Inject;
import jakarta.annotation.Nullable;

@ExtensionImpl
public class LombokElementFinder extends PsiElementFinder {

  private final JavaFileManager myFileManager;
  private final Project myProject;

  @Inject
  public LombokElementFinder(Project project, JavaFileManager fileManager) {
    myFileManager = fileManager;
    myProject = project;
  }

  @Nullable
  @Override
  public PsiClass findClass(@Nonnull String qualifiedName, @Nonnull GlobalSearchScope scope) {
    if (!LombokLibraryUtil.hasLombokLibrary(myProject)) {
      return null;
    }

    final int lastDot = qualifiedName.lastIndexOf('.');
    if (lastDot < 0) {
      return null;
    }

    final String parentName = qualifiedName.substring(0, lastDot);
    final String shortName = qualifiedName.substring(lastDot + 1);

    if (shortName.isEmpty() || parentName.isEmpty()) {
      return null;
    }

    final PsiClass parentClass = myFileManager.findClass(parentName, scope);
    if (null != parentClass) {
      return parentClass.findInnerClassByName(shortName, false);
    }

    return null;
  }

  @Override
  @Nonnull
  public PsiClass[] findClasses(@Nonnull String qualifiedName, @Nonnull GlobalSearchScope scope) {
    return PsiClass.EMPTY_ARRAY;
  }
}
