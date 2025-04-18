package de.plushnikov.intellij.plugin.processor;

import com.intellij.java.language.psi.*;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.psi.PsiElement;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.util.lang.StringUtil;
import de.plushnikov.intellij.plugin.LombokClassNames;
import de.plushnikov.intellij.plugin.problem.LombokProblem;
import de.plushnikov.intellij.plugin.problem.ProblemValidationSink;
import de.plushnikov.intellij.plugin.util.PsiAnnotationUtil;
import jakarta.annotation.Nonnull;

import java.util.Collection;
import java.util.Collections;

/**
 * Inspect and validate @Cleanup lombok annotation
 *
 * @author Plushnikov Michail
 */
@ExtensionImpl(id = "CleanupProcessor", order = "after DelegateMethodProcessor")
public class CleanupProcessor extends AbstractProcessor {

  public CleanupProcessor() {
    super(PsiElement.class, LombokClassNames.CLEANUP);
  }

  @Nonnull
  @Override
  public Collection<PsiAnnotation> collectProcessedAnnotations(@Nonnull PsiClass psiClass) {
    return Collections.emptyList();
  }

  @Nonnull
  @Override
  public Collection<LombokProblem> verifyAnnotation(@Nonnull PsiAnnotation psiAnnotation) {
    // TODO warning: "You're assigning an auto-cleanup variable to something else. This is a bad idea."
    final ProblemValidationSink problemNewBuilder = new ProblemValidationSink();

    PsiLocalVariable psiVariable = PsiTreeUtil.getParentOfType(psiAnnotation, PsiLocalVariable.class);
    if (null != psiVariable) {
      final String cleanupName = PsiAnnotationUtil.getStringAnnotationValue(psiAnnotation, "value", "close");

      if (StringUtil.isEmptyOrSpaces(cleanupName)) {
        problemNewBuilder.addErrorMessage("inspection.message.cleanup.value.cannot.be.empty.string");
      } else {
        validateCleanUpMethodExists(psiVariable, cleanupName, problemNewBuilder);
      }

      validateInitializerExist(problemNewBuilder, psiVariable);

    } else {
      problemNewBuilder.addErrorMessage("inspection.message.cleanup.legal.only.on.local.variable.declarations");
    }

    return problemNewBuilder.getProblems();
  }

  private static void validateCleanUpMethodExists(@Nonnull PsiLocalVariable psiVariable,
                                                  @Nonnull String cleanupName,
                                                  @Nonnull ProblemValidationSink problemNewBuilder) {
    final PsiType psiType = psiVariable.getType();
    if (psiType instanceof PsiClassType psiClassType) {
      final PsiClass psiClassOfField = psiClassType.resolve();
      final PsiMethod[] methods;

      if (psiClassOfField != null) {
        methods = psiClassOfField.findMethodsByName(cleanupName, true);
        boolean hasCleanupMethod = false;
        for (PsiMethod method : methods) {
          if (0 == method.getParameterList().getParametersCount()) {
            hasCleanupMethod = true;
          }
        }

        if (!hasCleanupMethod) {
          problemNewBuilder.addErrorMessage("inspection.message.cleanup.method.s.not.found.on.target.class", cleanupName);
        }
      }
    } else {
      problemNewBuilder.addErrorMessage("inspection.message.cleanup.legal.only.on.local.variable.declaration.inside.block");
    }
  }

  private static void validateInitializerExist(@Nonnull ProblemValidationSink problemNewBuilder, @Nonnull PsiLocalVariable psiVariable) {
    if (!psiVariable.hasInitializer()) {
      problemNewBuilder.addErrorMessage("inspection.message.cleanup.variable.declarations.need.to.be.initialized");
    }
  }

}
