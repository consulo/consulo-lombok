package de.plushnikov.intellij.plugin.processor.clazz.fieldnameconstants;


import com.intellij.java.language.psi.*;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.psi.PsiElement;
import consulo.lombok.processor.ProcessorUtil;
import de.plushnikov.intellij.plugin.LombokClassNames;
import de.plushnikov.intellij.plugin.problem.ProblemProcessingSink;
import de.plushnikov.intellij.plugin.problem.ProblemSink;
import de.plushnikov.intellij.plugin.processor.LombokPsiElementUsage;
import de.plushnikov.intellij.plugin.processor.clazz.AbstractClassProcessor;
import de.plushnikov.intellij.plugin.processor.field.FieldNameConstantsFieldProcessor;
import de.plushnikov.intellij.plugin.thirdparty.LombokUtils;
import de.plushnikov.intellij.plugin.util.LombokProcessorUtil;
import de.plushnikov.intellij.plugin.util.PsiAnnotationSearchUtil;
import de.plushnikov.intellij.plugin.util.PsiClassUtil;
import jakarta.annotation.Nonnull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Inspect and validate @FieldNameConstants lombok annotation on a class
 * Creates string constants containing the field name for each field of this class
 * Used for lombok v1.16.22 to lombok v1.18.2 only!
 *
 * @author Plushnikov Michail
 */
@ExtensionImpl(id = "FieldNameConstantsOldProcessor", order = "after StandardExceptionProcessor")
public class FieldNameConstantsOldProcessor extends AbstractClassProcessor {

  public FieldNameConstantsOldProcessor() {
    super(PsiField.class, LombokClassNames.FIELD_NAME_CONSTANTS);
  }

  private static FieldNameConstantsFieldProcessor getFieldNameConstantsFieldProcessor() {
    return ProcessorUtil.getProcessor(FieldNameConstantsFieldProcessor.class);
  }

  @Override
  protected boolean supportAnnotationVariant(@Nonnull PsiAnnotation psiAnnotation) {
    // old version of @FieldNameConstants has attributes "prefix" and "suffix", the new one not
    return null != psiAnnotation.findAttributeValue("prefix");
  }

  @Override
  protected boolean validate(@Nonnull PsiAnnotation psiAnnotation, @Nonnull PsiClass psiClass, @Nonnull ProblemSink builder) {
    final boolean result = validateAnnotationOnRightType(psiClass, builder) && LombokProcessorUtil.isLevelVisible(psiAnnotation);
    if (result) {
      final Collection<PsiField> psiFields = filterFields(psiClass);
      for (PsiField psiField : psiFields) {
        FieldNameConstantsFieldProcessor.checkIfFieldNameIsValidAndWarn(psiAnnotation, psiField, builder);
      }
    }
    return result;
  }

  private static boolean validateAnnotationOnRightType(@Nonnull PsiClass psiClass, @Nonnull ProblemSink builder) {
    boolean result = true;
    if (psiClass.isAnnotationType() || psiClass.isInterface()) {
      builder.addErrorMessage("inspection.message.field.name.constants.only.supported.on.class.enum.or.field.type");
      result = false;
    }
    return result;
  }

  @Override
  protected void generatePsiElements(@Nonnull PsiClass psiClass, @Nonnull PsiAnnotation psiAnnotation, @Nonnull List<? super PsiElement> target) {
    final Collection<PsiField> psiFields = filterFields(psiClass);
    for (PsiField psiField : psiFields) {
      if (FieldNameConstantsFieldProcessor.checkIfFieldNameIsValidAndWarn(psiAnnotation, psiField, new ProblemProcessingSink())) {
        target.add(FieldNameConstantsFieldProcessor.createFieldNameConstant(psiField, psiClass, psiAnnotation));
      }
    }
  }

  @Nonnull
  private static Collection<PsiField> filterFields(@Nonnull PsiClass psiClass) {
    final Collection<PsiField> psiFields = new ArrayList<>();

    FieldNameConstantsFieldProcessor fieldProcessor = getFieldNameConstantsFieldProcessor();
    for (PsiField psiField : PsiClassUtil.collectClassFieldsIntern(psiClass)) {
      boolean useField = true;
      PsiModifierList modifierList = psiField.getModifierList();
      if (null != modifierList) {
        //Skip static fields.
        useField = !modifierList.hasModifierProperty(PsiModifier.STATIC);
        //Skip fields having same annotation already
        useField &= PsiAnnotationSearchUtil.isNotAnnotatedWith(psiField, fieldProcessor.getSupportedAnnotationClasses());
        //Skip fields that start with $
        useField &= !psiField.getName().startsWith(LombokUtils.LOMBOK_INTERN_FIELD_MARKER);
      }

      if (useField) {
        psiFields.add(psiField);
      }
    }
    return psiFields;
  }

  @Override
  public LombokPsiElementUsage checkFieldUsage(@Nonnull PsiField psiField, @Nonnull PsiAnnotation psiAnnotation) {
    final PsiClass containingClass = psiField.getContainingClass();
    if (null != containingClass) {
      if (PsiClassUtil.getNames(filterFields(containingClass)).contains(psiField.getName())) {
        return LombokPsiElementUsage.USAGE;
      }
    }
    return LombokPsiElementUsage.NONE;
  }

}
