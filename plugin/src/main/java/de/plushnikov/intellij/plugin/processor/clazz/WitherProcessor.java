package de.plushnikov.intellij.plugin.processor.clazz;

import com.intellij.java.language.psi.*;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.psi.PsiElement;
import consulo.lombok.processor.ProcessorUtil;
import de.plushnikov.intellij.plugin.LombokClassNames;
import de.plushnikov.intellij.plugin.problem.ProblemSink;
import de.plushnikov.intellij.plugin.processor.LombokPsiElementUsage;
import de.plushnikov.intellij.plugin.processor.field.AccessorsInfo;
import de.plushnikov.intellij.plugin.processor.field.WitherFieldProcessor;
import de.plushnikov.intellij.plugin.thirdparty.LombokUtils;
import de.plushnikov.intellij.plugin.util.LombokProcessorUtil;
import de.plushnikov.intellij.plugin.util.PsiAnnotationSearchUtil;
import de.plushnikov.intellij.plugin.util.PsiClassUtil;
import jakarta.annotation.Nonnull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@ExtensionImpl(id = "WitherProcessor", order = "after ToStringProcessor")
public class WitherProcessor extends AbstractClassProcessor {
  private static final String BUILDER_DEFAULT_ANNOTATION = LombokClassNames.BUILDER_DEFAULT;

  public WitherProcessor() {
    super(PsiMethod.class, LombokClassNames.WITHER, LombokClassNames.WITH);
  }

  private static WitherFieldProcessor getWitherFieldProcessor() {
    return ProcessorUtil.getProcessor(WitherFieldProcessor.class);
  }

  @Override
  protected Collection<String> getNamesOfPossibleGeneratedElements(@Nonnull PsiClass psiClass, @Nonnull PsiAnnotation psiAnnotation) {
    Collection<String> result = new ArrayList<>();

    final Collection<? extends PsiVariable> possibleWithElements = getPossibleWithElements(psiClass);
    if (!possibleWithElements.isEmpty()) {
      final AccessorsInfo accessorsInfo = AccessorsInfo.buildFor(psiClass).withFluent(false);
      for (PsiVariable possibleWithElement : possibleWithElements) {
        result.add(LombokUtils.getWitherName(possibleWithElement, accessorsInfo));
      }
    }

    return result;
  }

  @Nonnull
  private static Collection<? extends PsiVariable> getPossibleWithElements(@Nonnull PsiClass psiClass) {
    if (psiClass.isRecord()) {
      return List.of(psiClass.getRecordComponents());
    }
    else {
      return PsiClassUtil.collectClassFieldsIntern(psiClass);
    }
  }

  @Override
  protected boolean validate(@Nonnull PsiAnnotation psiAnnotation, @Nonnull PsiClass psiClass, @Nonnull ProblemSink builder) {
    validateAnnotationOnRightType(psiClass, builder);
    validateVisibility(psiAnnotation, builder);
    if (builder.success()) {
      WitherFieldProcessor.validConstructor(psiClass, builder);
    }
    return builder.success();
  }

  private static void validateAnnotationOnRightType(@Nonnull PsiClass psiClass, @Nonnull ProblemSink builder) {
    if (psiClass.isAnnotationType() || psiClass.isInterface() || psiClass.isEnum()) {
      builder.addErrorMessage("inspection.message.wither.only.supported.on.class.or.field");
      builder.markFailed();
    }
  }

  private static void validateVisibility(@Nonnull PsiAnnotation psiAnnotation, @Nonnull ProblemSink builder) {
    if (null == LombokProcessorUtil.getMethodModifier(psiAnnotation)) {
      builder.markFailed();
    }
  }

  @Override
  protected void generatePsiElements(@Nonnull PsiClass psiClass,
                                     @Nonnull PsiAnnotation psiAnnotation,
                                     @Nonnull List<? super PsiElement> target) {
    final String methodVisibility = LombokProcessorUtil.getMethodModifier(psiAnnotation);
    if (methodVisibility != null) {
      final AccessorsInfo accessorsInfo = AccessorsInfo.buildFor(psiClass).withFluent(false);
      target.addAll(createFieldWithers(psiClass, methodVisibility, accessorsInfo));
    }
  }

  @Nonnull
  private static Collection<PsiMethod> createFieldWithers(@Nonnull PsiClass psiClass,
                                                          @Nonnull String methodModifier,
                                                          @Nonnull AccessorsInfo accessors) {
    Collection<PsiMethod> result = new ArrayList<>();

    final Collection<PsiField> witherFields = getWitherFields(psiClass);

    for (PsiField witherField : witherFields) {
      PsiMethod method = getWitherFieldProcessor().createWitherMethod(witherField, methodModifier, accessors);
      if (method != null) {
        result.add(method);
      }
    }

    return result;
  }

  @Nonnull
  private static Collection<PsiField> getWitherFields(@Nonnull PsiClass psiClass) {
    Collection<PsiField> witherFields = new ArrayList<>();

    for (PsiField psiField : psiClass.getFields()) {
      boolean createWither = true;
      PsiModifierList modifierList = psiField.getModifierList();
      if (null != modifierList) {
        // Skip static fields.
        createWither = !modifierList.hasModifierProperty(PsiModifier.STATIC);
        // Skip final fields that are initialized and not annotated with @Builder.Default
        createWither &= !(modifierList.hasModifierProperty(PsiModifier.FINAL) && psiField.hasInitializer() &&
                          PsiAnnotationSearchUtil.findAnnotation(psiField, BUILDER_DEFAULT_ANNOTATION) == null);
        // Skip fields that start with $
        createWither &= !psiField.getName().startsWith(LombokUtils.LOMBOK_INTERN_FIELD_MARKER);
        // Skip fields having Wither annotation already
        createWither &= !PsiAnnotationSearchUtil.isAnnotatedWith(psiField, LombokClassNames.WITHER, LombokClassNames.WITH);
      }
      if (createWither) {
        witherFields.add(psiField);
      }
    }
    return witherFields;
  }

  @Override
  public LombokPsiElementUsage checkFieldUsage(@Nonnull PsiField psiField, @Nonnull PsiAnnotation psiAnnotation) {
    final PsiClass containingClass = psiField.getContainingClass();
    if (null != containingClass) {
      final Collection<PsiField> witherFields = getWitherFields(containingClass);
      if (PsiClassUtil.getNames(witherFields).contains(psiField.getName())) {
        return LombokPsiElementUsage.READ_WRITE;
      }
    }
    return LombokPsiElementUsage.NONE;
  }
}
