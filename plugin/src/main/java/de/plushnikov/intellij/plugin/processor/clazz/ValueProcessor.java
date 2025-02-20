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
import de.plushnikov.intellij.plugin.processor.clazz.constructor.AbstractConstructorClassProcessor;
import de.plushnikov.intellij.plugin.processor.clazz.constructor.AllArgsConstructorProcessor;
import de.plushnikov.intellij.plugin.processor.clazz.constructor.NoArgsConstructorProcessor;
import de.plushnikov.intellij.plugin.util.PsiAnnotationSearchUtil;
import de.plushnikov.intellij.plugin.util.PsiAnnotationUtil;
import de.plushnikov.intellij.plugin.util.PsiClassUtil;
import jakarta.annotation.Nonnull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author twillouer
 */
@ExtensionImpl(id = "ValueProcessor", order = "after SuperBuilderProcessor")
public class ValueProcessor extends AbstractClassProcessor {

  public ValueProcessor() {
    super(PsiMethod.class, LombokClassNames.VALUE);
  }

  private static ToStringProcessor getToStringProcessor() {
    return ProcessorUtil.getProcessor(ToStringProcessor.class);
  }

  private static AllArgsConstructorProcessor getAllArgsConstructorProcessor() {
    return ProcessorUtil.getProcessor(AllArgsConstructorProcessor.class);
  }

  private static NoArgsConstructorProcessor getNoArgsConstructorProcessor() {
    return ProcessorUtil.getProcessor(NoArgsConstructorProcessor.class);
  }

  private static GetterProcessor getGetterProcessor() {
    return ProcessorUtil.getProcessor(GetterProcessor.class);
  }

  private static EqualsAndHashCodeProcessor getEqualsAndHashCodeProcessor() {
    return ProcessorUtil.getProcessor(EqualsAndHashCodeProcessor.class);
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

    return result;
  }

  private static String getStaticConstructorNameValue(@Nonnull PsiAnnotation psiAnnotation) {
    return PsiAnnotationUtil.getStringAnnotationValue(psiAnnotation, "staticConstructor", "");
  }

  @Override
  protected boolean validate(@Nonnull PsiAnnotation psiAnnotation, @Nonnull PsiClass psiClass, @Nonnull ProblemSink builder) {
    validateAnnotationOnRightType(psiClass, builder);

    if (builder.deepValidation()) {
      if (PsiAnnotationSearchUtil.isNotAnnotatedWith(psiClass, LombokClassNames.EQUALS_AND_HASHCODE)) {
        getEqualsAndHashCodeProcessor().validateCallSuperParamExtern(psiAnnotation, psiClass, builder);
      }
    }
    return builder.success();
  }

  private static void validateAnnotationOnRightType(@Nonnull PsiClass psiClass, @Nonnull ProblemSink builder) {
    if (psiClass.isAnnotationType() || psiClass.isInterface() || psiClass.isEnum()) {
      builder.addErrorMessage("inspection.message.value.only.supported.on.class.type");
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
    if (PsiAnnotationSearchUtil.isNotAnnotatedWith(psiClass, LombokClassNames.EQUALS_AND_HASHCODE)) {
      target.addAll(getEqualsAndHashCodeProcessor().createEqualAndHashCode(psiClass, psiAnnotation));
    }
    if (PsiAnnotationSearchUtil.isNotAnnotatedWith(psiClass, LombokClassNames.TO_STRING)) {
      target.addAll(getToStringProcessor().createToStringMethod(psiClass, psiAnnotation));
    }
    // create required constructor only if there are no other constructor annotations
    if (PsiAnnotationSearchUtil.isNotAnnotatedWith(psiClass, LombokClassNames.NO_ARGS_CONSTRUCTOR,
                                                   LombokClassNames.REQUIRED_ARGS_CONSTRUCTOR, LombokClassNames.ALL_ARGS_CONSTRUCTOR,
                                                   LombokClassNames.BUILDER)) {
      final Collection<PsiMethod> definedConstructors = PsiClassUtil.collectClassConstructorIntern(psiClass);
      filterToleratedElements(definedConstructors);

      final String staticName = getStaticConstructorNameValue(psiAnnotation);
      final Collection<PsiField> requiredFields = AbstractConstructorClassProcessor.getAllFields(psiClass);

      if (getAllArgsConstructorProcessor().validateIsConstructorNotDefined(psiClass, staticName, requiredFields,
                                                                           new ProblemProcessingSink())) {
        target.addAll(
          getAllArgsConstructorProcessor().createAllArgsConstructor(psiClass, PsiModifier.PUBLIC, psiAnnotation, staticName, requiredFields,
                                                                    true));
      }
    }

    if (shouldGenerateExtraNoArgsConstructor(psiClass)) {
      target.addAll(getNoArgsConstructorProcessor().createNoArgsConstructor(psiClass, PsiModifier.PRIVATE, psiAnnotation, true));
    }
  }

  @Nonnull
  @Override
  public Collection<PsiAnnotation> collectProcessedAnnotations(@Nonnull PsiClass psiClass) {
    final Collection<PsiAnnotation> result = super.collectProcessedAnnotations(psiClass);
    addClassAnnotation(result, psiClass, LombokClassNames.NON_FINAL, LombokClassNames.PACKAGE_PRIVATE);
    addFieldsAnnotation(result, psiClass, LombokClassNames.NON_FINAL, LombokClassNames.PACKAGE_PRIVATE);
    return result;
  }

  @Override
  public LombokPsiElementUsage checkFieldUsage(@Nonnull PsiField psiField, @Nonnull PsiAnnotation psiAnnotation) {
    return LombokPsiElementUsage.READ_WRITE;
  }
}
