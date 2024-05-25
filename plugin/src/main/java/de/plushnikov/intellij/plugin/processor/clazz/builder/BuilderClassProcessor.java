package de.plushnikov.intellij.plugin.processor.clazz.builder;

import com.intellij.java.language.psi.PsiAnnotation;
import com.intellij.java.language.psi.PsiClass;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.psi.PsiElement;
import de.plushnikov.intellij.plugin.LombokClassNames;
import de.plushnikov.intellij.plugin.problem.ProblemSink;
import de.plushnikov.intellij.plugin.processor.clazz.AbstractClassProcessor;
import de.plushnikov.intellij.plugin.processor.handler.BuilderHandler;
import jakarta.annotation.Nonnull;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Inspect and validate @Builder lombok annotation on a class
 * Creates inner class for a builder pattern
 *
 * @author Tomasz Kalkosiński
 * @author Michail Plushnikov
 */
@ExtensionImpl
public class BuilderClassProcessor extends AbstractClassProcessor {

  public BuilderClassProcessor() {
    super(PsiClass.class, LombokClassNames.BUILDER);
  }

  private static BuilderHandler getBuilderHandler() {
    return new BuilderHandler();
  }

  @Override
  protected Collection<String> getNamesOfPossibleGeneratedElements(@Nonnull PsiClass psiClass, @Nonnull PsiAnnotation psiAnnotation) {
    final String innerBuilderClassName = BuilderHandler.getBuilderClassName(psiClass, psiAnnotation, null);
    return Collections.singleton(innerBuilderClassName);
  }

  @Override
  protected boolean validate(@Nonnull PsiAnnotation psiAnnotation, @Nonnull PsiClass psiClass, @Nonnull ProblemSink builder) {
    return getBuilderHandler().validate(psiClass, psiAnnotation, builder);
  }

  @Override
  protected void generatePsiElements(@Nonnull PsiClass psiClass, @Nonnull PsiAnnotation psiAnnotation, @Nonnull List<? super PsiElement> target) {
    getBuilderHandler().createBuilderClassIfNotExist(psiClass, null, psiAnnotation).ifPresent(target::add);
  }
}
