package de.plushnikov.intellij.plugin.processor;

import com.intellij.java.language.psi.PsiAnnotation;
import com.intellij.java.language.psi.PsiClass;
import com.intellij.java.language.psi.PsiField;
import consulo.annotation.component.ComponentScope;
import consulo.annotation.component.ExtensionAPI;
import consulo.language.psi.PsiElement;
import consulo.util.collection.ContainerUtil;
import de.plushnikov.intellij.plugin.problem.LombokProblem;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author Plushnikov Michail
 */
@ExtensionAPI(ComponentScope.APPLICATION)
public interface Processor {
  @Nonnull
  String[] getSupportedAnnotationClasses();

  @Nonnull
  Class<? extends PsiElement> getSupportedClass();

  default boolean isSupportedAnnotationFQN(String annotationFQN) {
    return ContainerUtil.exists(getSupportedAnnotationClasses(), annotationFQN::equals);
  }

  default boolean isSupportedClass(Class<? extends PsiElement> someClass) {
    return getSupportedClass().equals(someClass);
  }

  @Nonnull
  Collection<LombokProblem> verifyAnnotation(@Nonnull PsiAnnotation psiAnnotation);

  @Nonnull
  default List<? super PsiElement> process(@Nonnull PsiClass psiClass) {
    return process(psiClass, null);
  }

  @Nonnull
  default List<? super PsiElement> process(@Nonnull PsiClass psiClass, @Nullable String nameHint) {
    return Collections.emptyList();
  }

  LombokPsiElementUsage checkFieldUsage(@Nonnull PsiField psiField, @Nonnull PsiAnnotation psiAnnotation);
}
