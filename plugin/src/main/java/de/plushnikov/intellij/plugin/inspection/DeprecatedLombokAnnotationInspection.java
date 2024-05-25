package de.plushnikov.intellij.plugin.inspection;

import com.intellij.java.analysis.impl.codeInsight.intention.AddAnnotationFix;
import com.intellij.java.language.psi.JavaElementVisitor;
import com.intellij.java.language.psi.PsiAnnotation;
import com.intellij.java.language.psi.PsiModifierListOwner;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.editor.inspection.ProblemHighlightType;
import consulo.language.editor.inspection.ProblemsHolder;
import consulo.language.psi.PsiElementVisitor;
import consulo.language.psi.util.PsiTreeUtil;
import de.plushnikov.intellij.plugin.LombokBundle;
import de.plushnikov.intellij.plugin.LombokClassNames;
import jakarta.annotation.Nonnull;

/**
 * @author Plushnikov Michail
 */
@ExtensionImpl
public class DeprecatedLombokAnnotationInspection extends LombokJavaInspectionBase {

  @Nonnull
  @Override
  public String getShortName() {
    return "DeprecatedLombok";
  }

  @Nonnull
  @Override
  protected PsiElementVisitor createVisitor(@Nonnull final ProblemsHolder holder, final boolean isOnTheFly) {
    return new LombokElementVisitor(holder);
  }

  @Nonnull
  @Override
  public String getDisplayName() {
    return LombokBundle.message("inspection.deprecated.lombok.display.name");
  }

  private static class LombokElementVisitor extends JavaElementVisitor {
    private final ProblemsHolder holder;

    LombokElementVisitor(ProblemsHolder holder) {
      this.holder = holder;
    }

    @Override
    public void visitAnnotation(final @Nonnull PsiAnnotation annotation) {
      checkFor("lombok.experimental.Builder", LombokClassNames.BUILDER, annotation);
      checkFor("lombok.experimental.Value", LombokClassNames.VALUE, annotation);
      checkFor("lombok.experimental.Wither", LombokClassNames.WITH, annotation);
    }

    private void checkFor(String deprecatedAnnotationFQN, String newAnnotationFQN, PsiAnnotation psiAnnotation) {
      if (psiAnnotation.hasQualifiedName(deprecatedAnnotationFQN)) {
        final PsiModifierListOwner listOwner = PsiTreeUtil.getParentOfType(psiAnnotation, PsiModifierListOwner.class, false);
        if (null != listOwner) {

          holder.registerProblem(psiAnnotation,
                                 LombokBundle
                                   .message("inspection.message.lombok.annotation.deprecated.not.supported", deprecatedAnnotationFQN,
                                            newAnnotationFQN),
                                 ProblemHighlightType.ERROR,
                                 new AddAnnotationFix(newAnnotationFQN,
                                                      listOwner,
                                                      psiAnnotation.getParameterList().getAttributes(),
                                                      deprecatedAnnotationFQN));
        }
      }
    }
  }
}
