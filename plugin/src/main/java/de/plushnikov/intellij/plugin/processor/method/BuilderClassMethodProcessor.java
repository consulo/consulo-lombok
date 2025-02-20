package de.plushnikov.intellij.plugin.processor.method;

import com.intellij.java.language.psi.PsiAnnotation;
import com.intellij.java.language.psi.PsiClass;
import com.intellij.java.language.psi.PsiMethod;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.psi.PsiElement;
import de.plushnikov.intellij.plugin.LombokClassNames;
import de.plushnikov.intellij.plugin.problem.ProblemSink;
import de.plushnikov.intellij.plugin.processor.handler.BuilderHandler;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.List;
import java.util.Objects;

/**
 * Inspect and validate @Builder lombok annotation on a method
 * Creates inner class for a builder pattern
 *
 * @author Tomasz Kalkosiński
 * @author Michail Plushnikov
 */
@ExtensionImpl(id = "BuilderClassMethodProcessor", order = "after BuilderProcessor")
public class BuilderClassMethodProcessor extends AbstractMethodProcessor {

  public BuilderClassMethodProcessor() {
    super(PsiClass.class, LombokClassNames.BUILDER);
  }

  /**
   * Checks the given annotation to be supported 'Builder' annotation
   */
  @Override
  protected boolean checkAnnotationFQN(@Nonnull PsiClass psiClass, @Nonnull PsiAnnotation psiAnnotation, @Nonnull PsiMethod psiMethod) {
    return BuilderHandler.checkAnnotationFQN(psiClass, psiAnnotation, psiMethod);
  }

  @Override
  protected boolean possibleToGenerateElementNamed(@Nullable String nameHint, @Nonnull PsiClass psiClass,
                                                   @Nonnull PsiAnnotation psiAnnotation, @Nonnull PsiMethod psiMethod) {
    if (null == nameHint) {
      return true;
    }

    final String innerBuilderClassName = BuilderHandler.getBuilderClassName(psiClass, psiAnnotation, psiMethod);
    return Objects.equals(nameHint, innerBuilderClassName);
  }

  @Override
  protected boolean validate(@Nonnull PsiAnnotation psiAnnotation, @Nonnull PsiMethod psiMethod, @Nonnull ProblemSink problemSink) {
    return getHandler().validate(psiMethod, psiAnnotation, problemSink);
  }

  @Override
  protected void processIntern(@Nonnull PsiMethod psiMethod, @Nonnull PsiAnnotation psiAnnotation, @Nonnull List<? super PsiElement> target) {
    final PsiClass psiClass = psiMethod.getContainingClass();
    if (null != psiClass) {
      final BuilderHandler builderHandler = getHandler();
      builderHandler.createBuilderClassIfNotExist(psiClass, psiMethod, psiAnnotation).ifPresent(target::add);
    }
  }

  private static BuilderHandler getHandler() {
    return new BuilderHandler();
  }
}
