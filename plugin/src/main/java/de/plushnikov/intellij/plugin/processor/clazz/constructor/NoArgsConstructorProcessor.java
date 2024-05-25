package de.plushnikov.intellij.plugin.processor.clazz.constructor;

import com.intellij.java.language.psi.PsiAnnotation;
import com.intellij.java.language.psi.PsiClass;
import com.intellij.java.language.psi.PsiField;
import com.intellij.java.language.psi.PsiMethod;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.psi.PsiElement;
import de.plushnikov.intellij.plugin.LombokClassNames;
import de.plushnikov.intellij.plugin.problem.ProblemSink;
import de.plushnikov.intellij.plugin.processor.LombokPsiElementUsage;
import de.plushnikov.intellij.plugin.quickfix.PsiQuickFixFactory;
import de.plushnikov.intellij.plugin.util.LombokProcessorUtil;
import de.plushnikov.intellij.plugin.util.PsiAnnotationUtil;
import de.plushnikov.intellij.plugin.util.PsiClassUtil;
import jakarta.annotation.Nonnull;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author Plushnikov Michail
 */
@ExtensionImpl
public final class NoArgsConstructorProcessor extends AbstractConstructorClassProcessor {
  public NoArgsConstructorProcessor() {
    super(LombokClassNames.NO_ARGS_CONSTRUCTOR, PsiMethod.class);
  }

  @Override
  protected boolean validate(@Nonnull PsiAnnotation psiAnnotation, @Nonnull PsiClass psiClass, @Nonnull ProblemSink problemSink) {
    boolean result;

    result = super.validate(psiAnnotation, psiClass, problemSink);

    if (!isForceConstructor(psiAnnotation)) {
      final String staticConstructorName = getStaticConstructorName(psiAnnotation);
      result &= validateIsConstructorNotDefined(psiClass, staticConstructorName, Collections.emptyList(), problemSink);

      if (problemSink.deepValidation()) {
        final Collection<PsiField> requiredFields = getRequiredFields(psiClass, true);
        if (!requiredFields.isEmpty()) {
          problemSink.addErrorMessage("inspection.message.constructor.noargs.needs.to.be.forced")
            .withLocalQuickFixes(() -> PsiQuickFixFactory.createChangeAnnotationParameterFix(psiAnnotation, "force", "true"));
        }
      }
    }

    return result;
  }

  @Nonnull
  public Collection<PsiMethod> createNoArgsConstructor(@Nonnull PsiClass psiClass,
                                                       @Nonnull String methodVisibility,
                                                       @Nonnull PsiAnnotation psiAnnotation) {
    final boolean forceConstructorWithJavaDefaults = isForceConstructor(psiAnnotation);
    return createNoArgsConstructor(psiClass, methodVisibility, psiAnnotation, forceConstructorWithJavaDefaults);
  }

  @Nonnull
  public Collection<PsiMethod> createNoArgsConstructor(@Nonnull PsiClass psiClass,
                                                       @Nonnull String methodVisibility,
                                                       @Nonnull PsiAnnotation psiAnnotation,
                                                       boolean withJavaDefaults) {
    final Collection<PsiField> params = getConstructorFields(psiClass, withJavaDefaults);
    return createConstructorMethod(psiClass, methodVisibility, psiAnnotation, withJavaDefaults, params);
  }

  private static boolean isForceConstructor(@Nonnull PsiAnnotation psiAnnotation) {
    return PsiAnnotationUtil.getBooleanAnnotationValue(psiAnnotation, "force", false);
  }

  @Override
  protected void generatePsiElements(@Nonnull PsiClass psiClass,
                                     @Nonnull PsiAnnotation psiAnnotation,
                                     @Nonnull List<? super PsiElement> target) {
    final String methodVisibility = LombokProcessorUtil.getAccessVisibility(psiAnnotation);
    if (null != methodVisibility) {
      target.addAll(createNoArgsConstructor(psiClass, methodVisibility, psiAnnotation));
    }
  }

  @Override
  public LombokPsiElementUsage checkFieldUsage(@Nonnull PsiField psiField, @Nonnull PsiAnnotation psiAnnotation) {
    final PsiClass containingClass = psiField.getContainingClass();
    if (null != containingClass) {

      final boolean forceConstructorWithJavaDefaults = isForceConstructor(psiAnnotation);
      final Collection<PsiField> params = getConstructorFields(containingClass, forceConstructorWithJavaDefaults);

      if (PsiClassUtil.getNames(params).contains(psiField.getName())) {
        return LombokPsiElementUsage.WRITE;
      }
    }
    return LombokPsiElementUsage.NONE;
  }

  @Nonnull
  private Collection<PsiField> getConstructorFields(PsiClass containingClass, boolean forceConstructorWithJavaDefaults) {
    Collection<PsiField> params;
    if (forceConstructorWithJavaDefaults) {
      params = getRequiredFields(containingClass);
    }
    else {
      params = Collections.emptyList();
    }
    return params;
  }
}
