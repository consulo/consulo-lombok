package de.plushnikov.intellij.plugin.processor.clazz;

import com.intellij.java.language.psi.*;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.psi.PsiElement;
import consulo.lombok.processor.ProcessorUtil;
import de.plushnikov.intellij.plugin.LombokClassNames;
import de.plushnikov.intellij.plugin.problem.ProblemSink;
import de.plushnikov.intellij.plugin.processor.LombokPsiElementUsage;
import de.plushnikov.intellij.plugin.processor.field.AccessorsInfo;
import de.plushnikov.intellij.plugin.processor.field.GetterFieldProcessor;
import de.plushnikov.intellij.plugin.thirdparty.LombokUtils;
import de.plushnikov.intellij.plugin.util.*;
import jakarta.annotation.Nonnull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Inspect and validate @Getter lombok annotation on a class
 * Creates getter methods for fields of this class
 *
 * @author Plushnikov Michail
 */
@ExtensionImpl
public final class GetterProcessor extends AbstractClassProcessor {
  public GetterProcessor() {
    super(PsiMethod.class, LombokClassNames.GETTER);
  }

  private static GetterFieldProcessor getGetterFieldProcessor() {
    return ProcessorUtil.getProcessor(GetterFieldProcessor.class);
  }

  @Override
  protected Collection<String> getNamesOfPossibleGeneratedElements(@Nonnull PsiClass psiClass, @Nonnull PsiAnnotation psiAnnotation) {
    Collection<String> result = new ArrayList<>();

    final AccessorsInfo.AccessorsValues classAccessorsValues = AccessorsInfo.getAccessorsValues(psiClass);
    for (PsiField psiField : PsiClassUtil.collectClassFieldsIntern(psiClass)) {
      final AccessorsInfo accessorsInfo = AccessorsInfo.buildFor(psiField, classAccessorsValues);
      result.add(LombokUtils.getGetterName(psiField, accessorsInfo));
    }

    return result;
  }

  @Override
  protected boolean validate(@Nonnull PsiAnnotation psiAnnotation, @Nonnull PsiClass psiClass, @Nonnull ProblemSink builder) {
    validateAnnotationOnRightType(psiClass, builder);
    validateVisibility(psiAnnotation, builder);

    if (builder.deepValidation()) {
      if (PsiAnnotationUtil.getBooleanAnnotationValue(psiAnnotation, "lazy", false)) {
        builder.addWarningMessage("inspection.message.lazy.not.supported.for.getter.on.type");
      }
    }
    return builder.success();
  }

  private static void validateAnnotationOnRightType(@Nonnull PsiClass psiClass, @Nonnull ProblemSink builder) {
    if (psiClass.isAnnotationType() || psiClass.isInterface()) {
      builder.addErrorMessage("inspection.message.getter.only.supported.on.class.enum.or.field.type");
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
      target.addAll(createFieldGetters(psiClass, methodVisibility));
    }
  }

  @Nonnull
  public Collection<PsiMethod> createFieldGetters(@Nonnull PsiClass psiClass, @Nonnull String methodModifier) {
    Collection<PsiMethod> result = new ArrayList<>();
    final Collection<PsiField> getterFields = filterGetterFields(psiClass);
    GetterFieldProcessor fieldProcessor = getGetterFieldProcessor();
    for (PsiField getterField : getterFields) {
      result.add(fieldProcessor.createGetterMethod(getterField, psiClass, methodModifier));
    }
    return result;
  }

  @Nonnull
  private Collection<PsiField> filterGetterFields(@Nonnull PsiClass psiClass) {
    final Collection<PsiField> getterFields = new ArrayList<>();

    final Collection<PsiMethod> classMethods = PsiClassUtil.collectClassMethodsIntern(psiClass);
    filterToleratedElements(classMethods);

    final AccessorsInfo.AccessorsValues classAccessorsValues = AccessorsInfo.getAccessorsValues(psiClass);
    GetterFieldProcessor fieldProcessor = getGetterFieldProcessor();
    for (PsiField psiField : psiClass.getFields()) {
      boolean createGetter = true;
      PsiModifierList modifierList = psiField.getModifierList();
      if (null != modifierList) {
        //Skip static fields.
        createGetter = !modifierList.hasModifierProperty(PsiModifier.STATIC);
        //Skip fields having Getter annotation already
        createGetter &= PsiAnnotationSearchUtil.isNotAnnotatedWith(psiField, fieldProcessor.getSupportedAnnotationClasses());
        //Skip fields that start with $
        createGetter &= !psiField.getName().startsWith(LombokUtils.LOMBOK_INTERN_FIELD_MARKER);
        //Skip fields if a method with same name and arguments count already exists
        final AccessorsInfo accessorsInfo = AccessorsInfo.buildFor(psiField, classAccessorsValues);
        final Collection<String> methodNames =
          LombokUtils.toAllGetterNames(accessorsInfo, psiField.getName(), PsiTypes.booleanType().equals(psiField.getType()));
        for (String methodName : methodNames) {
          createGetter &= !PsiMethodUtil.hasSimilarMethod(classMethods, methodName, 0);
        }
      }

      if (createGetter) {
        getterFields.add(psiField);
      }
    }
    return getterFields;
  }

  @Override
  public LombokPsiElementUsage checkFieldUsage(@Nonnull PsiField psiField, @Nonnull PsiAnnotation psiAnnotation) {
    final PsiClass containingClass = psiField.getContainingClass();
    if (null != containingClass) {
      if (PsiClassUtil.getNames(filterGetterFields(containingClass)).contains(psiField.getName())) {
        return LombokPsiElementUsage.READ;
      }
    }
    return LombokPsiElementUsage.NONE;
  }
}
