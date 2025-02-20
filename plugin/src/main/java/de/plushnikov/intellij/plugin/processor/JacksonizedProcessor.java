package de.plushnikov.intellij.plugin.processor;

import com.intellij.java.language.psi.*;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.psi.PsiElement;
import consulo.language.psi.util.PsiTreeUtil;
import de.plushnikov.intellij.plugin.LombokClassNames;
import de.plushnikov.intellij.plugin.problem.LombokProblem;
import de.plushnikov.intellij.plugin.problem.ProblemSink;
import de.plushnikov.intellij.plugin.problem.ProblemValidationSink;
import de.plushnikov.intellij.plugin.util.PsiAnnotationSearchUtil;
import jakarta.annotation.Nonnull;

import java.util.Collection;
import java.util.Collections;

/**
 * Inspect and validate @Jacksonized lombok annotation
 *
 * @author Plushnikov Michail
 */
@ExtensionImpl(id = "JacksonizedProcessor", order = "after SynchronizedProcessor")
public class JacksonizedProcessor extends AbstractProcessor {

  public JacksonizedProcessor() {
    super(PsiElement.class, LombokClassNames.JACKSONIZED);
  }

  @Nonnull
  @Override
  public Collection<PsiAnnotation> collectProcessedAnnotations(@Nonnull PsiClass psiClass) {
    return Collections.emptyList();
  }

  @Nonnull
  @Override
  public Collection<LombokProblem> verifyAnnotation(@Nonnull PsiAnnotation psiAnnotation) {
    final ProblemValidationSink validationSink = new ProblemValidationSink();

    final PsiModifierListOwner psiModifierListOwner;
    final PsiMethod psiMethod = PsiTreeUtil.getParentOfType(psiAnnotation, PsiMethod.class);
    if (null != psiMethod) {
      psiModifierListOwner = psiMethod;
    }
    else {
      psiModifierListOwner = PsiTreeUtil.getParentOfType(psiAnnotation, PsiClass.class);
    }

    if (null != psiModifierListOwner) {
      validateAnnotationOwner(psiModifierListOwner, validationSink);
    }

    return validationSink.getProblems();
  }

  public static boolean validateAnnotationOwner(@Nonnull PsiModifierListOwner psiModifierListOwner, ProblemSink validationSink) {
    final boolean hasBuilder = PsiAnnotationSearchUtil.isAnnotatedWith(psiModifierListOwner, LombokClassNames.BUILDER);
    final boolean hasSuperBuilder = PsiAnnotationSearchUtil.isAnnotatedWith(psiModifierListOwner, LombokClassNames.SUPER_BUILDER);
    if (!hasBuilder && !hasSuperBuilder) {
      validationSink.addWarningMessage("inspection.message.jacksonized.requires.builder.superbuilder");
    }

    if (hasBuilder && hasSuperBuilder) {
      validationSink.addErrorMessage("inspection.message.jacksonized.cannot.process.both.builder.superbuilder");
      validationSink.markFailed();
    }

    boolean isAbstract = psiModifierListOwner.hasModifierProperty(PsiModifier.ABSTRACT);
    if (isAbstract) {
      validationSink.addErrorMessage("inspection.message.jacksonized.builder.on.abstract.classes");
      validationSink.markFailed();
    }

    final boolean hasJsonDeserialize =
      PsiAnnotationSearchUtil.isAnnotatedWith(psiModifierListOwner, "com.fasterxml.jackson.databind.annotation.JsonDeserialize");
    if (hasJsonDeserialize) {
      validationSink.addErrorMessage("inspection.message.jacksonized.jsondeserialize.already.exists");
      validationSink.markFailed();
    }
    return validationSink.success();
  }
}
