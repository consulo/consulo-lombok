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

import java.util.List;

/**
 * Inspect and validate @Builder lombok annotation on a static method.
 * Creates methods for a builder pattern for initializing a class.
 *
 * @author Tomasz Kalkosiński
 * @author Michail Plushnikov
 */
@ExtensionImpl(id = "BuilderMethodProcessor", order = "after BuilderClassMethodProcessor")
public class BuilderMethodProcessor extends AbstractMethodProcessor {

  public BuilderMethodProcessor() {
    super(PsiMethod.class, LombokClassNames.BUILDER);
  }

  @Override
  protected boolean validate(@Nonnull PsiAnnotation psiAnnotation, @Nonnull PsiMethod psiMethod, @Nonnull ProblemSink problemSink) {
    // we skip validation here, because it will be validated by other BuilderClassProcessor
    return true;//builderHandler.validate(psiMethod, psiAnnotation, builder);
  }

  /**
   * Checks the given annotation to be supported 'Builder' annotation
   */
  @Override
  protected boolean checkAnnotationFQN(@Nonnull PsiClass psiClass, @Nonnull PsiAnnotation psiAnnotation, @Nonnull PsiMethod psiMethod) {
    return BuilderHandler.checkAnnotationFQN(psiClass, psiAnnotation, psiMethod);
  }

  @Override
  protected void processIntern(@Nonnull PsiMethod psiMethod,
                               @Nonnull PsiAnnotation psiAnnotation,
                               @Nonnull List<? super PsiElement> target) {
    final PsiClass psiClass = psiMethod.getContainingClass();
    final BuilderHandler builderHandler = getHandler();
    if (null != psiClass) {

      PsiClass builderClass = builderHandler.getExistInnerBuilderClass(psiClass, psiMethod, psiAnnotation).orElse(null);
      if (null == builderClass) {
        // have to create full class (with all methods) here, or auto-completion doesn't work
        builderClass = builderHandler.createBuilderClass(psiClass, psiMethod, psiAnnotation);
      }

      target.addAll(
        builderHandler.createBuilderDefaultProviderMethodsIfNecessary(psiClass, null, builderClass, psiAnnotation));

      builderHandler.createBuilderMethodIfNecessary(psiClass, psiMethod, builderClass, psiAnnotation)
        .ifPresent(target::add);

      builderHandler.createToBuilderMethodIfNecessary(psiClass, psiMethod, builderClass, psiAnnotation)
        .ifPresent(target::add);
    }
  }

  private static BuilderHandler getHandler() {
    return new BuilderHandler();
  }
}
