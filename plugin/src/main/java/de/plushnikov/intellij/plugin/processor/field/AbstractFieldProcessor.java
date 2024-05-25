package de.plushnikov.intellij.plugin.processor.field;

import com.intellij.java.language.impl.psi.impl.RecordAugmentProvider;
import com.intellij.java.language.psi.*;
import com.intellij.java.language.psi.util.MethodSignatureBackedByPsiMethod;
import consulo.application.util.CachedValueProvider;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiModificationTracker;
import consulo.language.psi.util.LanguageCachedValueUtil;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.util.collection.ContainerUtil;
import de.plushnikov.intellij.plugin.LombokClassNames;
import de.plushnikov.intellij.plugin.problem.LombokProblem;
import de.plushnikov.intellij.plugin.problem.ProblemProcessingSink;
import de.plushnikov.intellij.plugin.problem.ProblemSink;
import de.plushnikov.intellij.plugin.problem.ProblemValidationSink;
import de.plushnikov.intellij.plugin.processor.AbstractProcessor;
import de.plushnikov.intellij.plugin.thirdparty.LombokCopyableAnnotations;
import de.plushnikov.intellij.plugin.thirdparty.LombokUtils;
import de.plushnikov.intellij.plugin.util.LombokProcessorUtil;
import de.plushnikov.intellij.plugin.util.PsiAnnotationSearchUtil;
import de.plushnikov.intellij.plugin.util.PsiClassUtil;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Base lombok processor class for field annotations
 *
 * @author Plushnikov Michail
 */
public abstract class AbstractFieldProcessor extends AbstractProcessor implements FieldProcessor {

  AbstractFieldProcessor(@Nonnull Class<? extends PsiElement> supportedClass,
                         @Nonnull String supportedAnnotationClass) {
    super(supportedClass, supportedAnnotationClass);
  }

  AbstractFieldProcessor(@Nonnull Class<? extends PsiElement> supportedClass,
                         @Nonnull String supportedAnnotationClass,
                         @Nonnull String equivalentAnnotationClass) {
    super(supportedClass, supportedAnnotationClass, equivalentAnnotationClass);
  }

  @Nonnull
  @Override
  public List<? super PsiElement> process(@Nonnull PsiClass psiClass, @Nullable String nameHint) {
    List<? super PsiElement> result = new ArrayList<>();
    Collection<PsiField> fields = psiClass.isRecord() ? RecordAugmentProvider.getFieldAugments(psiClass)
                                                      : PsiClassUtil.collectClassFieldsIntern(psiClass);
    for (PsiField psiField : fields) {
      PsiAnnotation psiAnnotation = PsiAnnotationSearchUtil.findAnnotation(psiField, getSupportedAnnotationClasses());
      if (null != psiAnnotation) {
        if (possibleToGenerateElementNamed(nameHint, psiClass, psiAnnotation, psiField)
            && validate(psiAnnotation, psiField, new ProblemProcessingSink())) {

          generatePsiElements(psiField, psiAnnotation, result);
        }
      }
    }
    return result;
  }

  private boolean possibleToGenerateElementNamed(@Nullable String nameHint, @Nonnull PsiClass psiClass,
                                                 @Nonnull PsiAnnotation psiAnnotation, @Nonnull PsiField psiField) {
    if (null == nameHint) {
      return true;
    }
    final Collection<String> namesOfGeneratedElements = getNamesOfPossibleGeneratedElements(psiClass, psiAnnotation, psiField);
    return namesOfGeneratedElements.isEmpty() || namesOfGeneratedElements.contains(nameHint);
  }

  protected abstract Collection<String> getNamesOfPossibleGeneratedElements(@Nonnull PsiClass psiClass,
                                                                   @Nonnull PsiAnnotation psiAnnotation,
                                                                   @Nonnull PsiField psiField);

  protected abstract void generatePsiElements(@Nonnull PsiField psiField,
                                              @Nonnull PsiAnnotation psiAnnotation,
                                              @Nonnull List<? super PsiElement> target);

  @Nonnull
  @Override
  public Collection<PsiAnnotation> collectProcessedAnnotations(@Nonnull PsiClass psiClass) {
    List<PsiAnnotation> result = new ArrayList<>();
    for (PsiField psiField : PsiClassUtil.collectClassFieldsIntern(psiClass)) {
      PsiAnnotation psiAnnotation = PsiAnnotationSearchUtil.findAnnotation(psiField, getSupportedAnnotationClasses());
      if (null != psiAnnotation) {
        result.add(psiAnnotation);
      }
    }
    return result;
  }

  @Nonnull
  @Override
  public Collection<LombokProblem> verifyAnnotation(@Nonnull PsiAnnotation psiAnnotation) {
    Collection<LombokProblem> result = Collections.emptyList();

    PsiField psiField = PsiTreeUtil.getParentOfType(psiAnnotation, PsiField.class);
    if (null != psiField) {
      ProblemValidationSink problemNewBuilder = new ProblemValidationSink();
      validate(psiAnnotation, psiField, problemNewBuilder);
      result = problemNewBuilder.getProblems();
    }

    return result;
  }

  protected abstract boolean validate(@Nonnull PsiAnnotation psiAnnotation, @Nonnull PsiField psiField, @Nonnull ProblemSink builder);

  protected void validateOnXAnnotations(@Nonnull PsiAnnotation psiAnnotation,
                                        @Nonnull PsiField psiField,
                                        @Nonnull ProblemSink problemSink,
                                        @Nonnull String parameterName) {
    if (problemSink.deepValidation()) {
      final @Nonnull List<PsiAnnotation> copyableAnnotations = LombokCopyableAnnotations.BASE_COPYABLE.collectCopyableAnnotations(psiField);

      if (!copyableAnnotations.isEmpty()) {
        final Iterable<String> onXAnnotations = LombokProcessorUtil.getOnX(psiAnnotation, parameterName);
        List<String> copyableAnnotationsFQNs = ContainerUtil.map(copyableAnnotations, PsiAnnotation::getQualifiedName);
        for (String copyableAnnotationFQN : copyableAnnotationsFQNs) {
          for (String onXAnnotation : onXAnnotations) {
            if (onXAnnotation.startsWith(copyableAnnotationFQN)) {
              problemSink.addErrorMessage("inspection.message.annotation.copy.duplicate", copyableAnnotationFQN);
            }
          }
        }
      }

      if (psiField.isDeprecated()) {
        final Iterable<String> onMethodAnnotations = LombokProcessorUtil.getOnX(psiAnnotation, "onMethod");
        if (ContainerUtil.exists(onMethodAnnotations, CommonClassNames.JAVA_LANG_DEPRECATED::equals)) {
          problemSink.addErrorMessage("inspection.message.annotation.copy.duplicate", CommonClassNames.JAVA_LANG_DEPRECATED);
        }
      }
    }
  }

  protected boolean validateExistingMethods(@Nonnull PsiField psiField,
                                            @Nonnull ProblemSink builder,
                                            boolean isGetter) {

    final PsiClass psiClass = psiField.getContainingClass();
    if (null != psiClass) {
      //cache signatures to speed up editing of big files, where getName goes in psi tree
      List<MethodSignatureBackedByPsiMethod> ownSignatures = LanguageCachedValueUtil.getCachedValue(psiClass, () -> {
        List<MethodSignatureBackedByPsiMethod> signatures =
          ContainerUtil.map(PsiClassUtil.collectClassMethodsIntern(psiClass),
                            m -> MethodSignatureBackedByPsiMethod.create(m, PsiSubstitutor.EMPTY));
        return new CachedValueProvider.Result<>(signatures, PsiModificationTracker.MODIFICATION_COUNT);
      });

      final List<MethodSignatureBackedByPsiMethod> classMethods = new ArrayList<>(ownSignatures);

      final boolean isBoolean = PsiTypes.booleanType().equals(psiField.getType());
      final AccessorsInfo accessorsInfo = AccessorsInfo.buildFor(psiField);
      final String fieldName = psiField.getName();
      String accessorName = isGetter ? LombokUtils.toGetterName(accessorsInfo, fieldName, isBoolean)
                                     : LombokUtils.toSetterName(accessorsInfo, fieldName, isBoolean);
      int paramCount = isGetter ? 0 : 1;
      classMethods.removeIf(m -> m.getParameterTypes().length != paramCount || !accessorName.equals(m.getName()));

      classMethods.removeIf(definedMethod -> PsiAnnotationSearchUtil.isAnnotatedWith(definedMethod.getMethod(), LombokClassNames.TOLERATE));

      if (!classMethods.isEmpty()) {
        builder.addWarningMessage("inspection.message.not.generated.s.method.with.similar.name.s.already.exists",
                                  accessorName, accessorName);
        return false;
      }
    }
    return true;
  }
}
