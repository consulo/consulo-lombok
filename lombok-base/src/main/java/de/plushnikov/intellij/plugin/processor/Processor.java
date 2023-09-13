package de.plushnikov.intellij.plugin.processor;

import com.intellij.java.language.psi.PsiAnnotation;
import com.intellij.java.language.psi.PsiClass;
import com.intellij.java.language.psi.PsiField;
import consulo.language.psi.PsiElement;
import consulo.util.collection.ContainerUtil;
import de.plushnikov.intellij.plugin.problem.LombokProblem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author Plushnikov Michail
 */
public interface Processor {
  @NotNull
  String @NotNull[] getSupportedAnnotationClasses();

  @NotNull
  Class<? extends PsiElement> getSupportedClass();

  default boolean isSupportedAnnotationFQN(String annotationFQN) {
    return ContainerUtil.exists(getSupportedAnnotationClasses(), annotationFQN::equals);
  }

  default boolean isSupportedClass(Class<? extends PsiElement> someClass) {
    return getSupportedClass().equals(someClass);
  }

  @NotNull
  Collection<LombokProblem> verifyAnnotation(@NotNull PsiAnnotation psiAnnotation);

  @NotNull
  default List<? super PsiElement> process(@NotNull PsiClass psiClass) {
    return process(psiClass, null);
  }

  @NotNull
  default List<? super PsiElement> process(@NotNull PsiClass psiClass, @Nullable String nameHint) {
    return Collections.emptyList();
  }

  LombokPsiElementUsage checkFieldUsage(@NotNull PsiField psiField, @NotNull PsiAnnotation psiAnnotation);
}
