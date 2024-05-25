package de.plushnikov.intellij.plugin.processor.clazz.builder;

import com.intellij.java.language.psi.*;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.psi.PsiElement;
import consulo.util.lang.StringUtil;
import de.plushnikov.intellij.plugin.LombokClassNames;
import de.plushnikov.intellij.plugin.problem.ProblemSink;
import de.plushnikov.intellij.plugin.processor.LombokPsiElementUsage;
import de.plushnikov.intellij.plugin.processor.clazz.AbstractClassProcessor;
import de.plushnikov.intellij.plugin.processor.handler.BuilderHandler;
import de.plushnikov.intellij.plugin.processor.handler.SuperBuilderHandler;
import de.plushnikov.intellij.plugin.util.PsiAnnotationSearchUtil;
import jakarta.annotation.Nonnull;

import java.util.Collection;
import java.util.List;

/**
 * Inspect and validate @SuperBuilder lombok annotation on a class.
 * Creates methods for a @SuperBuilder pattern for initializing a class.
 *
 * @author Michail Plushnikov
 */
@ExtensionImpl
public class SuperBuilderProcessor extends AbstractClassProcessor {

  public SuperBuilderProcessor() {
    super(PsiMethod.class, LombokClassNames.SUPER_BUILDER);
  }

  protected SuperBuilderHandler getBuilderHandler() {
    return new SuperBuilderHandler();
  }

  @Override
  protected Collection<String> getNamesOfPossibleGeneratedElements(@Nonnull PsiClass psiClass, @Nonnull PsiAnnotation psiAnnotation) {
    final BuilderHandler builderHandler = getBuilderHandler();

    final String builderMethodName = builderHandler.getBuilderMethodName(psiAnnotation);
    final String constructorName = StringUtil.notNullize(psiClass.getName());
    return List.of(builderMethodName, BuilderHandler.TO_BUILDER_METHOD_NAME, constructorName);
  }

  @Nonnull
  @Override
  public Collection<PsiAnnotation> collectProcessedAnnotations(@Nonnull PsiClass psiClass) {
    final Collection<PsiAnnotation> result = super.collectProcessedAnnotations(psiClass);
    addJacksonizedAnnotation(psiClass, result);
    addFieldsAnnotation(result, psiClass, BuilderProcessor.SINGULAR_CLASS, BuilderProcessor.BUILDER_DEFAULT_CLASS);
    return result;
  }

  private static void addJacksonizedAnnotation(@Nonnull PsiClass psiClass, Collection<PsiAnnotation> result) {
    final PsiAnnotation jacksonizedAnnotation = PsiAnnotationSearchUtil.findAnnotation(psiClass, LombokClassNames.JACKSONIZED);
    if(null!=jacksonizedAnnotation) {
      result.add(jacksonizedAnnotation);
    }
  }

  @Override
  protected boolean validate(@Nonnull PsiAnnotation psiAnnotation, @Nonnull PsiClass psiClass, @Nonnull ProblemSink builder) {
    // we skip validation here, because it will be validated by other BuilderClassProcessor
    return true;//builderHandler.validate(psiClass, psiAnnotation, builder);
  }

  @Override
  protected void generatePsiElements(@Nonnull PsiClass psiClass, @Nonnull PsiAnnotation psiAnnotation, @Nonnull List<? super PsiElement> target) {
    SuperBuilderHandler builderHandler = getBuilderHandler();
    final String builderClassName = builderHandler.getBuilderClassName(psiClass);
    final PsiClass builderBaseClass = psiClass.findInnerClassByName(builderClassName, false);
    if (null != builderBaseClass) {
      final PsiClassType psiTypeBaseWithGenerics = SuperBuilderHandler.getTypeWithWildcardsForSuperBuilderTypeParameters(builderBaseClass);

      builderHandler.createBuilderBasedConstructor(psiClass, builderBaseClass, psiAnnotation, psiTypeBaseWithGenerics)
        .ifPresent(target::add);

      // skip generation of builder methods, if class is abstract
      if (!psiClass.hasModifierProperty(PsiModifier.ABSTRACT)) {
        final String builderImplClassName = builderHandler.getBuilderImplClassName(psiClass);
        final PsiClass builderImplClass = psiClass.findInnerClassByName(builderImplClassName, false);

        if (null != builderImplClass) {
          builderHandler.createBuilderMethodIfNecessary(psiClass, builderBaseClass, builderImplClass, psiAnnotation, psiTypeBaseWithGenerics)
            .ifPresent(target::add);

          SuperBuilderHandler.createToBuilderMethodIfNecessary(psiClass, builderBaseClass, builderImplClass, psiAnnotation, psiTypeBaseWithGenerics)
            .ifPresent(target::add);
        }
      }
    }
  }

  @Override
  public LombokPsiElementUsage checkFieldUsage(@Nonnull PsiField psiField, @Nonnull PsiAnnotation psiAnnotation) {
    return LombokPsiElementUsage.READ_WRITE;
  }
}
