package de.plushnikov.intellij.plugin.processor.clazz.fieldnameconstants;

import com.intellij.java.language.psi.PsiAnnotation;
import com.intellij.java.language.psi.PsiClass;
import com.intellij.java.language.psi.PsiField;
import com.intellij.java.language.psi.PsiMember;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.psi.PsiElement;
import de.plushnikov.intellij.plugin.LombokClassNames;
import de.plushnikov.intellij.plugin.problem.ProblemProcessingSink;
import de.plushnikov.intellij.plugin.problem.ProblemSink;
import de.plushnikov.intellij.plugin.processor.handler.FieldNameConstantsHandler;
import de.plushnikov.intellij.plugin.util.PsiAnnotationSearchUtil;
import de.plushnikov.intellij.plugin.util.PsiAnnotationUtil;
import de.plushnikov.intellij.plugin.util.PsiClassUtil;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.*;

/**
 * Updates fields of existing inner class type in class annotated by FieldNameConstants
 *
 * @author alanachtenberg
 */
@ExtensionImpl
public class FieldNameConstantsPredefinedInnerClassFieldProcessor extends AbstractFieldNameConstantsProcessor {

  public FieldNameConstantsPredefinedInnerClassFieldProcessor() {
    super(PsiField.class, LombokClassNames.FIELD_NAME_CONSTANTS);
  }

  @Nonnull
  @Override
  public List<? super PsiElement> process(@Nonnull PsiClass psiClass, @Nullable String nameHint) {
    if (psiClass.getParent() instanceof PsiClass parentClass) {
      PsiAnnotation psiAnnotation = PsiAnnotationSearchUtil.findAnnotation(parentClass, getSupportedAnnotationClasses());
      if (null != psiAnnotation && supportAnnotationVariant(psiAnnotation)) {
        ProblemProcessingSink problemBuilder = new ProblemProcessingSink();
        if (super.validate(psiAnnotation, parentClass, problemBuilder)) {
          final String typeName = FieldNameConstantsHandler.getTypeName(parentClass, psiAnnotation);
          if (typeName.equals(psiClass.getName())
            && possibleToGenerateElementNamed(nameHint, psiClass, psiAnnotation)
            && validate(psiAnnotation, parentClass, problemBuilder)) {

            List<? super PsiElement> result = new ArrayList<>();
            generatePsiElements(parentClass, psiClass, psiAnnotation, result);
            return result;
          }
        }
      }
    }
    return Collections.emptyList();
  }

  @Override
  protected boolean validate(@Nonnull PsiAnnotation psiAnnotation, @Nonnull PsiClass psiClass, @Nonnull ProblemSink builder) {
    final String typeName = FieldNameConstantsHandler.getTypeName(psiClass, psiAnnotation);
    Optional<PsiClass> innerClass = PsiClassUtil.getInnerClassInternByName(psiClass, typeName);
    if (innerClass.isPresent()) {
      final boolean asEnum = PsiAnnotationUtil.getBooleanAnnotationValue(psiAnnotation, "asEnum", false);
      if (innerClass.get().isEnum() != asEnum) {
        builder.addErrorMessage("inspection.message.field.name.constants.inner.type", asEnum);
        return false;
      }
    }
    return true;
  }

  @Override
  protected void generatePsiElements(@Nonnull PsiClass psiClass, @Nonnull PsiAnnotation psiAnnotation, @Nonnull List<? super PsiElement> target) {
    //do nothing
  }

  private void generatePsiElements(@Nonnull PsiClass psiClass, @Nonnull PsiClass existingInnerClass, @Nonnull PsiAnnotation psiAnnotation, @Nonnull List<? super PsiElement> target) {
    final Collection<PsiMember> psiMembers = filterMembers(psiClass, psiAnnotation);
    if (!psiMembers.isEmpty()) {
      List<PsiField> newFields = FieldNameConstantsHandler.createFields(existingInnerClass, psiMembers);
      target.addAll(newFields);
    }
  }
}
