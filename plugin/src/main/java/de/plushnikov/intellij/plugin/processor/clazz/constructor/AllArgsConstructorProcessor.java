package de.plushnikov.intellij.plugin.processor.clazz.constructor;

import com.intellij.java.language.psi.*;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.psi.PsiElement;
import de.plushnikov.intellij.plugin.LombokClassNames;
import de.plushnikov.intellij.plugin.problem.ProblemSink;
import de.plushnikov.intellij.plugin.processor.LombokPsiElementUsage;
import de.plushnikov.intellij.plugin.util.LombokProcessorUtil;
import de.plushnikov.intellij.plugin.util.PsiClassUtil;
import jakarta.annotation.Nonnull;

import java.util.Collection;
import java.util.List;

/**
 * @author Plushnikov Michail
 */
@ExtensionImpl
public final class AllArgsConstructorProcessor extends AbstractConstructorClassProcessor {
  public AllArgsConstructorProcessor() {
    super(LombokClassNames.ALL_ARGS_CONSTRUCTOR, PsiMethod.class);
  }

  @Override
  protected boolean validate(@Nonnull PsiAnnotation psiAnnotation, @Nonnull PsiClass psiClass, @Nonnull ProblemSink builder) {
    boolean result;

    result = super.validate(psiAnnotation, psiClass, builder);

    final Collection<PsiField> allNotInitializedNotStaticFields = getAllFields(psiClass);
    final String staticConstructorName = getStaticConstructorName(psiAnnotation);
    result &= validateIsConstructorNotDefined(psiClass, staticConstructorName, allNotInitializedNotStaticFields, builder);

    return result;
  }

  @Nonnull
  public Collection<PsiMethod> createAllArgsConstructor(@Nonnull PsiClass psiClass, @Nonnull String methodVisibility, @Nonnull PsiAnnotation psiAnnotation) {
    final Collection<PsiField> allNotInitializedNotStaticFields = getAllNotInitializedAndNotStaticFields(psiClass);
    return createConstructorMethod(psiClass, methodVisibility, psiAnnotation, false, allNotInitializedNotStaticFields);
  }

  @Override
  protected void generatePsiElements(@Nonnull PsiClass psiClass, @Nonnull PsiAnnotation psiAnnotation, @Nonnull List<? super PsiElement> target) {
    final String methodVisibility = LombokProcessorUtil.getAccessVisibility(psiAnnotation);
    if (null != methodVisibility) {
      final String staticConstructorName = getStaticConstructorName(psiAnnotation);
      target.addAll(createAllArgsConstructor(psiClass, methodVisibility, psiAnnotation, staticConstructorName));
    }
  }

  @Nonnull
  private Collection<PsiMethod> createAllArgsConstructor(PsiClass psiClass, String methodVisibility, PsiAnnotation psiAnnotation, String staticName) {
    final Collection<PsiField> allNotInitializedNotStaticFields = getAllFields(psiClass);
    return createAllArgsConstructor(psiClass, methodVisibility, psiAnnotation, staticName, allNotInitializedNotStaticFields, false);
  }

  @Nonnull
  public Collection<PsiMethod> createAllArgsConstructor(@Nonnull PsiClass psiClass, @PsiModifier.ModifierConstant @Nonnull String methodModifier, @Nonnull PsiAnnotation psiAnnotation, String staticName, Collection<PsiField> allNotInitializedNotStaticFields, boolean skipConstructorIfAnyConstructorExists) {
    return createConstructorMethod(psiClass, methodModifier, psiAnnotation, false, allNotInitializedNotStaticFields, staticName, skipConstructorIfAnyConstructorExists);
  }

  @Override
  public LombokPsiElementUsage checkFieldUsage(@Nonnull PsiField psiField, @Nonnull PsiAnnotation psiAnnotation) {
    final PsiClass containingClass = psiField.getContainingClass();
    if (null != containingClass) {
      if (PsiClassUtil.getNames(getAllNotInitializedAndNotStaticFields(containingClass)).contains(psiField.getName())) {
        return LombokPsiElementUsage.WRITE;
      }
    }
    return LombokPsiElementUsage.NONE;
  }
}
