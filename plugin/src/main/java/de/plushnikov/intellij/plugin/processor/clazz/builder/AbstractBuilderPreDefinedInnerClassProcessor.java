package de.plushnikov.intellij.plugin.processor.clazz.builder;

import com.intellij.java.language.psi.PsiAnnotation;
import com.intellij.java.language.psi.PsiClass;
import com.intellij.java.language.psi.PsiMethod;
import consulo.language.psi.PsiElement;
import de.plushnikov.intellij.plugin.problem.LombokProblem;
import de.plushnikov.intellij.plugin.problem.ProblemProcessingSink;
import de.plushnikov.intellij.plugin.problem.ProblemSink;
import de.plushnikov.intellij.plugin.processor.clazz.AbstractClassProcessor;
import de.plushnikov.intellij.plugin.processor.handler.BuilderHandler;
import de.plushnikov.intellij.plugin.util.PsiAnnotationSearchUtil;
import de.plushnikov.intellij.plugin.util.PsiClassUtil;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.*;

public abstract class AbstractBuilderPreDefinedInnerClassProcessor extends AbstractClassProcessor {

  AbstractBuilderPreDefinedInnerClassProcessor(@Nonnull Class<? extends PsiElement> supportedClass,
                                               @Nonnull String supportedAnnotationClass) {
    super(supportedClass, supportedAnnotationClass);
  }

  @Nonnull
  @Override
  public List<? super PsiElement> process(@Nonnull PsiClass psiClass, @Nullable String nameHint) {
    final Optional<PsiClass> parentClass = getSupportedParentClass(psiClass);
    final Optional<PsiAnnotation> builderAnnotation = parentClass.map(this::getSupportedAnnotation);
    if (builderAnnotation.isPresent()) {
      final PsiClass psiParentClass = parentClass.get();
      final PsiAnnotation psiBuilderAnnotation = builderAnnotation.get();
      // use parent class as source!
      if (validate(psiBuilderAnnotation, psiParentClass, new ProblemProcessingSink())) {
        return processAnnotation(psiParentClass, null, psiBuilderAnnotation, psiClass, nameHint);
      }
    } else if (parentClass.isPresent()) {
      final PsiClass psiParentClass = parentClass.get();
      final Collection<PsiMethod> psiMethods = PsiClassUtil.collectClassMethodsIntern(psiParentClass);
      for (PsiMethod psiMethod : psiMethods) {
        final PsiAnnotation psiBuilderAnnotation = PsiAnnotationSearchUtil.findAnnotation(psiMethod, getSupportedAnnotationClasses());
        if (null != psiBuilderAnnotation) {
          final String builderClassNameOfThisMethod = BuilderHandler.getBuilderClassName(psiParentClass, psiBuilderAnnotation, psiMethod);
          // check we found right method for this existing builder class
          if (Objects.equals(builderClassNameOfThisMethod, psiClass.getName())) {
            return processAnnotation(psiParentClass, psiMethod, psiBuilderAnnotation, psiClass, nameHint);
          }
        }
      }
    }
    return Collections.emptyList();
  }

  private List<? super PsiElement> processAnnotation(@Nonnull PsiClass psiParentClass, @Nullable PsiMethod psiParentMethod,
                                                     @Nonnull PsiAnnotation psiAnnotation, @Nonnull PsiClass psiClass,
                                                     @Nullable String nameHint) {
    // use parent class as source!
    final String builderClassName = BuilderHandler.getBuilderClassName(psiParentClass, psiAnnotation, psiParentMethod);

    List<? super PsiElement> result = new ArrayList<>();
    // apply only to inner BuilderClass
    if (builderClassName.equals(psiClass.getName())
      && possibleToGenerateElementNamed(nameHint, psiClass, psiAnnotation)) {
      result.addAll(generatePsiElements(psiParentClass, psiParentMethod, psiAnnotation, psiClass));
    }
    return result;
  }

  protected BuilderHandler getBuilderHandler() {
    return new BuilderHandler();
  }

  protected abstract Collection<? extends PsiElement> generatePsiElements(@Nonnull PsiClass psiParentClass, @Nullable PsiMethod psiParentMethod, @Nonnull PsiAnnotation psiAnnotation, @Nonnull PsiClass psiBuilderClass);

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
