package de.plushnikov.intellij.plugin.processor.clazz.builder;

import com.intellij.java.language.psi.PsiAnnotation;
import com.intellij.java.language.psi.PsiClass;
import com.intellij.java.language.psi.PsiMethod;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.psi.PsiElement;
import de.plushnikov.intellij.plugin.LombokClassNames;
import jakarta.annotation.Nonnull;

import java.util.Collection;

/**
 * Creates methods for a builder inner class if it is predefined.
 *
 * @author Michail Plushnikov
 */
@ExtensionImpl(id = "SuperBuilderPreDefinedInnerClassMethodProcessor", order = "after SuperBuilderPreDefinedInnerClassFieldProcessor")
public class SuperBuilderPreDefinedInnerClassMethodProcessor extends AbstractSuperBuilderPreDefinedInnerClassProcessor {

  public SuperBuilderPreDefinedInnerClassMethodProcessor() {
    super(PsiMethod.class, LombokClassNames.SUPER_BUILDER);
  }

  @Override
  protected Collection<? extends PsiElement> generatePsiElementsOfBaseBuilderClass(@Nonnull PsiClass psiParentClass, @Nonnull PsiAnnotation psiAnnotation, @Nonnull PsiClass psiBuilderClass) {
    return getBuilderHandler().createAllMethodsOfBaseBuilder(psiParentClass, psiAnnotation, psiBuilderClass);
  }

  @Override
  protected Collection<? extends PsiElement> generatePsiElementsOfImplBuilderClass(@Nonnull PsiClass psiParentClass, @Nonnull PsiAnnotation psiAnnotation, @Nonnull PsiClass psiBuilderClass) {
    return getBuilderHandler().createAllMethodsOfImplBuilder(psiParentClass, psiAnnotation, psiBuilderClass);
  }

}
