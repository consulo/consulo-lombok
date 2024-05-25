package de.plushnikov.intellij.plugin.processor.clazz.fieldnameconstants;

import com.intellij.java.language.psi.*;
import consulo.language.psi.PsiElement;
import consulo.util.lang.StringUtil;
import de.plushnikov.intellij.plugin.LombokClassNames;
import de.plushnikov.intellij.plugin.problem.ProblemSink;
import de.plushnikov.intellij.plugin.processor.LombokPsiElementUsage;
import de.plushnikov.intellij.plugin.processor.clazz.AbstractClassProcessor;
import de.plushnikov.intellij.plugin.thirdparty.LombokUtils;
import de.plushnikov.intellij.plugin.util.LombokProcessorUtil;
import de.plushnikov.intellij.plugin.util.PsiAnnotationSearchUtil;
import de.plushnikov.intellij.plugin.util.PsiAnnotationUtil;
import de.plushnikov.intellij.plugin.util.PsiClassUtil;
import jakarta.annotation.Nonnull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class AbstractFieldNameConstantsProcessor extends AbstractClassProcessor {

  private static final String FIELD_NAME_CONSTANTS_INCLUDE = LombokClassNames.FIELD_NAME_CONSTANTS_INCLUDE;
  private static final String FIELD_NAME_CONSTANTS_EXCLUDE = LombokClassNames.FIELD_NAME_CONSTANTS_EXCLUDE;

  AbstractFieldNameConstantsProcessor(@Nonnull Class<? extends PsiElement> supportedClass, @Nonnull String supportedAnnotationClass) {
    super(supportedClass, supportedAnnotationClass);
  }

  @Override
  protected boolean supportAnnotationVariant(@Nonnull PsiAnnotation psiAnnotation) {
    // new version of @FieldNameConstants has an attribute "asEnum", the old one not
    return null != psiAnnotation.findAttributeValue("asEnum");
  }

  @Override
  protected boolean validate(@Nonnull PsiAnnotation psiAnnotation, @Nonnull PsiClass psiClass, @Nonnull ProblemSink builder) {
    return validateAnnotationOnRightType(psiClass, builder) && LombokProcessorUtil.isLevelVisible(psiAnnotation);
  }

  private static boolean validateAnnotationOnRightType(@Nonnull PsiClass psiClass, @Nonnull ProblemSink builder) {
    if (psiClass.isAnnotationType() || psiClass.isInterface()) {
      builder.addErrorMessage("inspection.message.field.name.constants.only.supported.on.class.or.enum");
      return false;
    }
    return true;
  }

  @Nonnull
  Collection<PsiMember> filterMembers(@Nonnull PsiClass psiClass, PsiAnnotation psiAnnotation) {
    final Collection<PsiMember> result = new ArrayList<>();

    final boolean onlyExplicitlyIncluded = PsiAnnotationUtil.getBooleanAnnotationValue(psiAnnotation, "onlyExplicitlyIncluded", false);

    Collection<? extends PsiMember> psiMembers;
    if(psiClass.isRecord()) {
      psiMembers = List.of(psiClass.getRecordComponents());
    }else{
      psiMembers =  PsiClassUtil.collectClassFieldsIntern(psiClass);
    }

    for (PsiMember psiMember : psiMembers) {
      boolean useField = true;
      PsiModifierList modifierList = psiMember.getModifierList();
      if (null != modifierList) {

        //Skip static fields.
        useField = !modifierList.hasModifierProperty(PsiModifier.STATIC);
        //Skip transient fields
        useField &= !modifierList.hasModifierProperty(PsiModifier.TRANSIENT);
      }
      //Skip fields that start with $
      useField &= !StringUtil.notNullize(psiMember.getName()).startsWith(LombokUtils.LOMBOK_INTERN_FIELD_MARKER);
      //Skip fields annotated with @FieldNameConstants.Exclude
      useField &= !PsiAnnotationSearchUtil.isAnnotatedWith(psiMember, FIELD_NAME_CONSTANTS_EXCLUDE);

      if (onlyExplicitlyIncluded) {
        //Only use fields annotated with @FieldNameConstants.Include, Include annotation overrides other rules
        useField = PsiAnnotationSearchUtil.isAnnotatedWith(psiMember, FIELD_NAME_CONSTANTS_INCLUDE);
      }

      if (useField) {
        result.add(psiMember);
      }
    }
    return result;
  }

  @Nonnull
  @Override
  public Collection<PsiAnnotation> collectProcessedAnnotations(@Nonnull PsiClass psiClass) {
    final Collection<PsiAnnotation> result = super.collectProcessedAnnotations(psiClass);
    addFieldsAnnotation(result, psiClass, FIELD_NAME_CONSTANTS_INCLUDE, FIELD_NAME_CONSTANTS_EXCLUDE);
    return result;
  }

  @Override
  public LombokPsiElementUsage checkFieldUsage(@Nonnull PsiField psiField, @Nonnull PsiAnnotation psiAnnotation) {
    final PsiClass containingClass = psiField.getContainingClass();
    if (null != containingClass) {
      if (PsiClassUtil.getNames(filterMembers(containingClass, psiAnnotation)).contains(psiField.getName())) {
        return LombokPsiElementUsage.USAGE;
      }
    }
    return LombokPsiElementUsage.NONE;
  }
}
