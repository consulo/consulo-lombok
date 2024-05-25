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
import jakarta.annotation.Nullable;

import java.util.Collection;
import java.util.List;

/**
 * @author Plushnikov Michail
 */
@ExtensionImpl
public final class RequiredArgsConstructorProcessor extends AbstractConstructorClassProcessor {
  public RequiredArgsConstructorProcessor() {
    super(LombokClassNames.REQUIRED_ARGS_CONSTRUCTOR, PsiMethod.class);
  }

  @Override
  protected boolean validate(@Nonnull PsiAnnotation psiAnnotation, @Nonnull PsiClass psiClass, @Nonnull ProblemSink builder) {
    boolean result;

    result = super.validate(psiAnnotation, psiClass, builder);

    final Collection<PsiField> allReqFields = getRequiredFields(psiClass);
    final String staticConstructorName = getStaticConstructorName(psiAnnotation);
    result &= validateIsConstructorNotDefined(psiClass, staticConstructorName, allReqFields, builder);

    return result;
  }

  @Override
  protected void generatePsiElements(@Nonnull PsiClass psiClass, @Nonnull PsiAnnotation psiAnnotation, @Nonnull List<? super PsiElement> target) {
    final String methodVisibility = LombokProcessorUtil.getAccessVisibility(psiAnnotation);
    if (null != methodVisibility) {
      target.addAll(
        createRequiredArgsConstructor(psiClass, methodVisibility, psiAnnotation, getStaticConstructorName(psiAnnotation), false));
    }
  }

  @Nonnull
  public Collection<PsiMethod> createRequiredArgsConstructor(@Nonnull PsiClass psiClass, @PsiModifier.ModifierConstant @Nonnull String methodModifier, @Nonnull PsiAnnotation psiAnnotation, @Nullable String staticName, boolean skipConstructorIfAnyConstructorExists) {
    final Collection<PsiField> allReqFields = getRequiredFields(psiClass);

    return createConstructorMethod(psiClass, methodModifier, psiAnnotation, false, allReqFields, staticName, skipConstructorIfAnyConstructorExists);
  }

  @Override
  public LombokPsiElementUsage checkFieldUsage(@Nonnull PsiField psiField, @Nonnull PsiAnnotation psiAnnotation) {
    final PsiClass containingClass = psiField.getContainingClass();
    if (null != containingClass) {
      if (PsiClassUtil.getNames(getRequiredFields(containingClass)).contains(psiField.getName())) {
        return LombokPsiElementUsage.WRITE;
      }
    }
    return LombokPsiElementUsage.NONE;
  }
}
