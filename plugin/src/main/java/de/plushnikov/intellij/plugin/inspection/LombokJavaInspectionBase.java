package de.plushnikov.intellij.plugin.inspection;

import com.intellij.java.analysis.codeInspection.AbstractBaseJavaLocalInspectionTool;
import com.intellij.java.language.JavaLanguage;
import consulo.language.Language;
import consulo.language.editor.inspection.LocalInspectionToolSession;
import consulo.language.editor.inspection.ProblemsHolder;
import consulo.language.editor.rawHighlight.HighlightDisplayLevel;
import consulo.language.psi.PsiElementVisitor;
import de.plushnikov.intellij.plugin.util.LombokLibraryUtil;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public abstract class LombokJavaInspectionBase extends AbstractBaseJavaLocalInspectionTool {
  @Nonnull
  @Override
  public PsiElementVisitor buildVisitorImpl(@Nonnull ProblemsHolder holder,
                                            boolean isOnTheFly,
                                            LocalInspectionToolSession session,
                                            Object o) {
    if (!LombokLibraryUtil.hasLombokLibrary(holder.getProject())) {
      return PsiElementVisitor.EMPTY_VISITOR;
    }

    return createVisitor(holder, isOnTheFly);
  }

  @Nullable
  @Override
  public Language getLanguage() {
    return JavaLanguage.INSTANCE;
  }

  @Nonnull
  @Override
  public String getGroupDisplayName() {
    return "Lombok";
  }

  @Override
  public boolean isEnabledByDefault() {
    return true;
  }

  @Nonnull
  @Override
  public HighlightDisplayLevel getDefaultLevel() {
    return HighlightDisplayLevel.WARNING;
  }

  @Nonnull
  protected abstract PsiElementVisitor createVisitor(@Nonnull ProblemsHolder holder, boolean isOnTheFly);
}
