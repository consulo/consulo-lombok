package de.plushnikov.intellij.plugin.processor.field;

import com.intellij.java.language.psi.*;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.psi.PsiElement;
import de.plushnikov.intellij.plugin.LombokClassNames;
import de.plushnikov.intellij.plugin.problem.ProblemSink;
import de.plushnikov.intellij.plugin.processor.LombokPsiElementUsage;
import de.plushnikov.intellij.plugin.processor.handler.DelegateHandler;
import jakarta.annotation.Nonnull;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Inspect and validate @Delegate lombok annotation on a field
 * Creates delegation methods for this field
 *
 * @author Plushnikov Michail
 */
@ExtensionImpl(id = "DelegateFieldProcessor", order = "after FieldNameConstantsPredefinedInnerClassFieldProcessor")
public class DelegateFieldProcessor extends AbstractFieldProcessor {

  public DelegateFieldProcessor() {
    super(PsiMethod.class, LombokClassNames.DELEGATE, LombokClassNames.EXPERIMENTAL_DELEGATE);
  }

  protected Collection<String> getNamesOfPossibleGeneratedElements(@Nonnull PsiClass psiClass,
                                                                   @Nonnull PsiAnnotation psiAnnotation,
                                                                   @Nonnull PsiField psiField) {
    return Collections.emptyList();
  }

  @Override
  protected boolean validate(@Nonnull PsiAnnotation psiAnnotation, @Nonnull PsiField psiField, @Nonnull ProblemSink builder) {
    final PsiType psiFieldType = psiField.getType();
    return DelegateHandler.validate(psiField, psiFieldType, psiAnnotation, builder);
  }

  @Override
  protected void generatePsiElements(@Nonnull PsiField psiField,
                                     @Nonnull PsiAnnotation psiAnnotation,
                                     @Nonnull List<? super PsiElement> target) {
    DelegateHandler.generateElements(psiField, psiField.getType(), psiAnnotation, target);
  }

  @Override
  public LombokPsiElementUsage checkFieldUsage(@Nonnull PsiField psiField, @Nonnull PsiAnnotation psiAnnotation) {
    return LombokPsiElementUsage.READ;
  }
}
