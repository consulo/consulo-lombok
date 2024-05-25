package de.plushnikov.intellij.plugin.processor.clazz.builder;

import com.intellij.java.language.psi.PsiAnnotation;
import com.intellij.java.language.psi.PsiClass;
import consulo.language.psi.PsiElement;
import de.plushnikov.intellij.plugin.problem.LombokProblem;
import de.plushnikov.intellij.plugin.problem.ProblemProcessingSink;
import de.plushnikov.intellij.plugin.problem.ProblemSink;
import de.plushnikov.intellij.plugin.processor.clazz.AbstractClassProcessor;
import de.plushnikov.intellij.plugin.processor.handler.SuperBuilderHandler;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.*;

public abstract class AbstractSuperBuilderPreDefinedInnerClassProcessor extends AbstractClassProcessor {

  AbstractSuperBuilderPreDefinedInnerClassProcessor(@Nonnull Class<? extends PsiElement> supportedClass,
                                                    @Nonnull String supportedAnnotationClass) {
    super(supportedClass, supportedAnnotationClass);
  }

  protected SuperBuilderHandler getBuilderHandler() {
    return new SuperBuilderHandler();
  }

  @Nonnull
  @Override
  public List<? super PsiElement> process(@Nonnull PsiClass psiClass, @Nullable String nameHint) {
    final Optional<PsiClass> parentClass = getSupportedParentClass(psiClass);
    final Optional<PsiAnnotation> psiAnnotation = parentClass.map(this::getSupportedAnnotation);
    if (psiAnnotation.isPresent()) {
      final PsiClass psiParentClass = parentClass.get();
      final PsiAnnotation psiBuilderAnnotation = psiAnnotation.get();
      // use parent class as source!
      if (validate(psiBuilderAnnotation, psiParentClass, new ProblemProcessingSink())) {
        return processAnnotation(psiParentClass, psiBuilderAnnotation, psiClass, nameHint);
      }
    }
    return Collections.emptyList();
  }

  private List<? super PsiElement> processAnnotation(@Nonnull PsiClass psiParentClass, @Nonnull PsiAnnotation psiAnnotation,
                                                     @Nonnull PsiClass psiClass, @Nullable String nameHint) {
    SuperBuilderHandler builderHandler = getBuilderHandler();
    // use parent class as source!
    final String builderBaseClassName = builderHandler.getBuilderClassName(psiParentClass);

    List<? super PsiElement> result = new ArrayList<>();
    // apply only to inner BuilderClass
    final String psiClassName = psiClass.getName();
    if (builderBaseClassName.equals(psiClassName) && possibleToGenerateElementNamed(nameHint, psiClass, psiAnnotation)) {
      result.addAll(generatePsiElementsOfBaseBuilderClass(psiParentClass, psiAnnotation, psiClass));
    } else {
      // use parent class as source!
      final String builderImplClassName = builderHandler.getBuilderImplClassName(psiParentClass);
      if (builderImplClassName.equals(psiClassName) && possibleToGenerateElementNamed(nameHint, psiClass, psiAnnotation)) {
        result.addAll(generatePsiElementsOfImplBuilderClass(psiParentClass, psiAnnotation, psiClass));
      }
    }
    return result;
  }

  protected abstract Collection<? extends PsiElement> generatePsiElementsOfBaseBuilderClass(@Nonnull PsiClass psiParentClass, @Nonnull PsiAnnotation psiAnnotation, @Nonnull PsiClass psiBuilderClass);

  protected abstract Collection<? extends PsiElement> generatePsiElementsOfImplBuilderClass(@Nonnull PsiClass psiParentClass, @Nonnull PsiAnnotation psiAnnotation, @Nonnull PsiClass psiBuilderClass);

  @Nonnull
  @Override
  public Collection<LombokProblem> verifyAnnotation(@Nonnull PsiAnnotation psiAnnotation) {
    //do nothing
    return Collections.emptySet();
  }

  @Override
  protected boolean validate(@Nonnull PsiAnnotation psiAnnotation, @Nonnull PsiClass psiClass, @Nonnull ProblemSink builder) {
    return getBuilderHandler().validate(psiClass, psiAnnotation, builder);
  }

  @Override
  protected void generatePsiElements(@Nonnull PsiClass psiClass, @Nonnull PsiAnnotation psiAnnotation, @Nonnull List<? super PsiElement> target) {
    //do nothing
  }
}
