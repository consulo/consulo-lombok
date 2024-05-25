package de.plushnikov.intellij.plugin.processor.method;

import com.intellij.java.language.psi.PsiAnnotation;
import com.intellij.java.language.psi.PsiClass;
import com.intellij.java.language.psi.PsiMethod;
import consulo.language.psi.PsiElement;
import consulo.language.psi.util.PsiTreeUtil;
import de.plushnikov.intellij.plugin.problem.LombokProblem;
import de.plushnikov.intellij.plugin.problem.ProblemProcessingSink;
import de.plushnikov.intellij.plugin.problem.ProblemSink;
import de.plushnikov.intellij.plugin.problem.ProblemValidationSink;
import de.plushnikov.intellij.plugin.processor.AbstractProcessor;
import de.plushnikov.intellij.plugin.util.PsiAnnotationSearchUtil;
import de.plushnikov.intellij.plugin.util.PsiClassUtil;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Base lombok processor class for method annotations
 *
 * @author Tomasz Kalkosiński
 */
public abstract class AbstractMethodProcessor extends AbstractProcessor implements MethodProcessor {

  AbstractMethodProcessor(@Nonnull Class<? extends PsiElement> supportedClass,
                          @Nonnull String supportedAnnotationClass) {
    super(supportedClass, supportedAnnotationClass);
  }

  AbstractMethodProcessor(@Nonnull Class<? extends PsiElement> supportedClass,
                          @Nonnull String supportedAnnotationClass,
                          @Nonnull String equivalentAnnotationClass) {
    super(supportedClass, supportedAnnotationClass, equivalentAnnotationClass);
  }

  @Nonnull
  @Override
  public List<? super PsiElement> process(@Nonnull PsiClass psiClass, @Nullable String nameHint) {
    List<? super PsiElement> result = new ArrayList<>();
    for (PsiMethod psiMethod : PsiClassUtil.collectClassMethodsIntern(psiClass)) {
      PsiAnnotation psiAnnotation = PsiAnnotationSearchUtil.findAnnotationByShortNameOnly(psiMethod, getSupportedAnnotationClasses());
      if (null != psiAnnotation && possibleToGenerateElementNamed(nameHint, psiClass, psiAnnotation, psiMethod)) {
        if (checkAnnotationFQN(psiClass, psiAnnotation, psiMethod)
            && validate(psiAnnotation, psiMethod, new ProblemProcessingSink())) {
          processIntern(psiMethod, psiAnnotation, result);
        }
      }
    }
    return result;
  }

  /**
   * Checks the given annotation to be supported annotation by this processor
   */
  protected boolean checkAnnotationFQN(@Nonnull PsiClass psiClass, @Nonnull PsiAnnotation psiAnnotation, @Nonnull PsiMethod psiMethod) {
    return PsiAnnotationSearchUtil.checkAnnotationHasOneOfFQNs(psiAnnotation, getSupportedAnnotationClasses());
  }

  protected boolean possibleToGenerateElementNamed(@Nullable String nameHint, @Nonnull PsiClass psiClass,
                                                   @Nonnull PsiAnnotation psiAnnotation, @Nonnull PsiMethod psiMethod) {
    return true;
  }

  @Nonnull
  @Override
  public Collection<PsiAnnotation> collectProcessedAnnotations(@Nonnull PsiClass psiClass) {
    List<PsiAnnotation> result = new ArrayList<>();
    for (PsiMethod psiMethod : PsiClassUtil.collectClassMethodsIntern(psiClass)) {
      PsiAnnotation psiAnnotation = PsiAnnotationSearchUtil.findAnnotation(psiMethod, getSupportedAnnotationClasses());
      if (null != psiAnnotation) {
        result.add(psiAnnotation);
      }
    }
    return result;
  }

  @Nonnull
  @Override
  public Collection<LombokProblem> verifyAnnotation(@Nonnull PsiAnnotation psiAnnotation) {
    Collection<LombokProblem> result = Collections.emptyList();

    PsiMethod psiMethod = PsiTreeUtil.getParentOfType(psiAnnotation, PsiMethod.class);
    if (null != psiMethod) {
      ProblemValidationSink problemNewBuilder = new ProblemValidationSink();
      validate(psiAnnotation, psiMethod, problemNewBuilder);
      result = problemNewBuilder.getProblems();
    }

    return result;
  }

  protected abstract boolean validate(@Nonnull PsiAnnotation psiAnnotation, @Nonnull PsiMethod psiMethod, @Nonnull ProblemSink problemSink);

  protected abstract void processIntern(PsiMethod psiMethod, PsiAnnotation psiAnnotation, List<? super PsiElement> target);
}
