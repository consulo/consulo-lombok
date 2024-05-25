package de.plushnikov.intellij.plugin.inspection;

import com.intellij.java.language.psi.*;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.editor.inspection.ProblemHighlightType;
import consulo.language.editor.inspection.ProblemsHolder;
import consulo.language.psi.PsiElementVisitor;
import de.plushnikov.intellij.plugin.LombokBundle;
import de.plushnikov.intellij.plugin.processor.clazz.log.Slf4jProcessor;
import de.plushnikov.intellij.plugin.quickfix.UseSlf4jAnnotationQuickFix;
import jakarta.annotation.Nonnull;

@ExtensionImpl
public class RedundantSlf4jDefinitionInspection extends LombokJavaInspectionBase {

  private static final String LOGGER_SLF4J_FQCN = Slf4jProcessor.LOGGER_TYPE;
  private static final String LOGGER_INITIALIZATION = "LoggerFactory.getLogger(%s.class)";

  @Nonnull
  @Override
  protected PsiElementVisitor createVisitor(@Nonnull ProblemsHolder holder, boolean isOnTheFly) {
    return new LombokDefinitionVisitor(holder);
  }

  @Nonnull
  @Override
  public String getDisplayName() {
    return LombokBundle.message("inspection.redundant.slf.4.j.definition.display.name");
  }

  @Nonnull
  @Override
  public String getShortName() {
    return "RedundantSlf4jDefinition";
  }

  private static class LombokDefinitionVisitor extends JavaElementVisitor {

    private final ProblemsHolder holder;

    LombokDefinitionVisitor(ProblemsHolder holder) {
      this.holder = holder;
    }

    @Override
    public void visitField(@Nonnull PsiField field) {
      super.visitField(field);
      findRedundantDefinition(field, field.getContainingClass());
    }

    private void findRedundantDefinition(PsiVariable field, PsiClass containingClass) {
      if (field.getType().equalsToText(LOGGER_SLF4J_FQCN)) {
        final PsiExpression initializer = field.getInitializer();
        if (initializer != null && containingClass != null) {
          if (initializer.getText().contains(String.format(LOGGER_INITIALIZATION, containingClass.getQualifiedName()))) {
            holder.registerProblem(field,
                                   LombokBundle.message("inspection.message.slf4j.logger.defined.explicitly"),
                                   ProblemHighlightType.WARNING,
                                   new UseSlf4jAnnotationQuickFix(field, containingClass));
          }
        }
      }
    }
  }
}
