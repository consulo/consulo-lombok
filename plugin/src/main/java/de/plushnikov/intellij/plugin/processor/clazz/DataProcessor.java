package de.plushnikov.intellij.plugin.processor.clazz;

import com.intellij.java.language.psi.*;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.psi.PsiElement;
import consulo.lombok.processor.ProcessorUtil;
import consulo.util.lang.StringUtil;
import de.plushnikov.intellij.plugin.LombokClassNames;
import de.plushnikov.intellij.plugin.problem.ProblemProcessingSink;
import de.plushnikov.intellij.plugin.problem.ProblemSink;
import de.plushnikov.intellij.plugin.processor.LombokPsiElementUsage;
import de.plushnikov.intellij.plugin.processor.clazz.constructor.NoArgsConstructorProcessor;
import de.plushnikov.intellij.plugin.processor.clazz.constructor.RequiredArgsConstructorProcessor;
import de.plushnikov.intellij.plugin.util.PsiAnnotationSearchUtil;
import de.plushnikov.intellij.plugin.util.PsiAnnotationUtil;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Plushnikov Michail
 */
@ExtensionImpl
public class DataProcessor extends AbstractClassProcessor {

  public DataProcessor() {
    super(PsiMethod.class, LombokClassNames.DATA);
  }

  private static ToStringProcessor getToStringProcessor() {
    return ProcessorUtil.getProcessor(ToStringProcessor.class);
  }

  private static NoArgsConstructorProcessor getNoArgsConstructorProcessor() {
    return ProcessorUtil.getProcessor(NoArgsConstructorProcessor.class);
  }

  private static GetterProcessor getGetterProcessor() {
    return ProcessorUtil.getProcessor(GetterProcessor.class);
  }

  private static SetterProcessor getSetterProcessor() {
    return ProcessorUtil.getProcessor(SetterProcessor.class);
  }

  private static EqualsAndHashCodeProcessor getEqualsAndHashCodeProcessor() {
    return ProcessorUtil.getProcessor(EqualsAndHashCodeProcessor.class);
  }

  private static RequiredArgsConstructorProcessor getRequiredArgsConstructorProcessor() {
    return ProcessorUtil.getProcessor(RequiredArgsConstructorProcessor.class);
  }

  @Override
  protected Collection<String> getNamesOfPossibleGeneratedElements(@Nonnull PsiClass psiClass, @Nonnull PsiAnnotation psiAnnotation) {
    Collection<String> result = new ArrayList<>();

    final String staticConstructorName = getStaticConstructorNameValue(psiAnnotation);
    if(StringUtil.isNotEmpty(staticConstructorName)) {
      result.add(staticConstructorName);
    }
    result.addAll(getNoArgsConstructorProcessor().getNamesOfPossibleGeneratedElements(psiClass, psiAnnotation));
    result.addAll(getToStringProcessor().getNamesOfPossibleGeneratedElements(psiClass, psiAnnotation));
    result.addAll(getEqualsAndHashCodeProcessor().getNamesOfPossibleGeneratedElements(psiClass, psiAnnotation));
    result.addAll(getGetterProcessor().getNamesOfPossibleGeneratedElements(psiClass, psiAnnotation));
    result.addAll(getSetterProcessor().getNamesOfPossibleGeneratedElements(psiClass, psiAnnotation));

    return result;
  }

  private static String getStaticConstructorNameValue(@Nonnull PsiAnnotation psiAnnotation) {
    return PsiAnnotationUtil.getStringAnnotationValue(psiAnnotation, "staticConstructor", "");
  }

  @Override
  protected boolean validate(@Nonnull PsiAnnotation psiAnnotation, @Nonnull PsiClass psiClass, @Nonnull ProblemSink builder) {
    validateAnnotationOnRightType(psiClass, builder);

    if (builder.deepValidation()) {
      final boolean hasNoEqualsAndHashCodeAnnotation =
        PsiAnnotationSearchUtil.isNotAnnotatedWith(psiClass, LombokClassNames.EQUALS_AND_HASHCODE);
      if (hasNoEqualsAndHashCodeAnnotation) {
        getEqualsAndHashCodeProcessor().validateCallSuperParamExtern(psiAnnotation, psiClass, builder);
      }

      final String staticName = getStaticConstructorNameValue(psiAnnotation);
      if (shouldGenerateRequiredArgsConstructor(psiClass, staticName)) {
        getRequiredArgsConstructorProcessor().validateBaseClassConstructor(psiClass, builder);
      }
    }
    return builder.success();
  }

  private static void validateAnnotationOnRightType(@Nonnull PsiClass psiClass, @Nonnull ProblemSink builder) {
    if (psiClass.isAnnotationType() || psiClass.isInterface() || psiClass.isEnum()) {
      builder.addErrorMessage("inspection.message.data.only.supported.on.class.type");
      builder.markFailed();
    }
  }

  @Override
  protected void generatePsiElements(@Nonnull PsiClass psiClass,
                                     @Nonnull PsiAnnotation psiAnnotation,
                                     @Nonnull List<? super PsiElement> target) {
    if (PsiAnnotationSearchUtil.isNotAnnotatedWith(psiClass, LombokClassNames.GETTER)) {
      target.addAll(getGetterProcessor().createFieldGetters(psiClass, PsiModifier.PUBLIC));
    }
    if (PsiAnnotationSearchUtil.isNotAnnotatedWith(psiClass, LombokClassNames.SETTER)) {
      target.addAll(getSetterProcessor().createFieldSetters(psiClass, PsiModifier.PUBLIC));
    }
    if (PsiAnnotationSearchUtil.isNotAnnotatedWith(psiClass, LombokClassNames.EQUALS_AND_HASHCODE)) {
      target.addAll(getEqualsAndHashCodeProcessor().createEqualAndHashCode(psiClass, psiAnnotation));
    }
    if (PsiAnnotationSearchUtil.isNotAnnotatedWith(psiClass, LombokClassNames.TO_STRING)) {
      target.addAll(getToStringProcessor().createToStringMethod(psiClass, psiAnnotation));
    }

    final boolean hasConstructorWithoutParameters;
    final String staticName = getStaticConstructorNameValue(psiAnnotation);
    if (shouldGenerateRequiredArgsConstructor(psiClass, staticName)) {
      target.addAll(
        getRequiredArgsConstructorProcessor().createRequiredArgsConstructor(psiClass, PsiModifier.PUBLIC, psiAnnotation, staticName, true));
      // if there are no required field, it will already have a default constructor without parameters
      hasConstructorWithoutParameters = getRequiredArgsConstructorProcessor().getRequiredFields(psiClass).isEmpty();
    }
    else {
      hasConstructorWithoutParameters = false;
    }

    if (!hasConstructorWithoutParameters && shouldGenerateExtraNoArgsConstructor(psiClass)) {
      target.addAll(getNoArgsConstructorProcessor().createNoArgsConstructor(psiClass, PsiModifier.PRIVATE, psiAnnotation, true));
    }
  }

  private static boolean shouldGenerateRequiredArgsConstructor(@Nonnull PsiClass psiClass, @Nullable String staticName) {
    boolean result = false;
    // create required constructor only if there are no other constructor annotations
    final boolean notAnnotatedWith = PsiAnnotationSearchUtil.isNotAnnotatedWith(psiClass,
                                                                                LombokClassNames.NO_ARGS_CONSTRUCTOR,
                                                                                LombokClassNames.REQUIRED_ARGS_CONSTRUCTOR,
                                                                                LombokClassNames.ALL_ARGS_CONSTRUCTOR,
                                                                                LombokClassNames.BUILDER,
                                                                                LombokClassNames.SUPER_BUILDER);
    if (notAnnotatedWith) {
      final RequiredArgsConstructorProcessor requiredArgsConstructorProcessor = getRequiredArgsConstructorProcessor();
      final Collection<PsiField> requiredFields = requiredArgsConstructorProcessor.getRequiredFields(psiClass);

      result = requiredArgsConstructorProcessor.validateIsConstructorNotDefined(
        psiClass, staticName, requiredFields, new ProblemProcessingSink());
    }
    return result;
  }

  @Override
  public LombokPsiElementUsage checkFieldUsage(@Nonnull PsiField psiField, @Nonnull PsiAnnotation psiAnnotation) {
    return LombokPsiElementUsage.READ_WRITE;
  }
}
