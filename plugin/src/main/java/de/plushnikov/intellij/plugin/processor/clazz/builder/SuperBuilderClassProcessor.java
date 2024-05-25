package de.plushnikov.intellij.plugin.processor.clazz.builder;

import com.intellij.java.language.psi.PsiAnnotation;
import com.intellij.java.language.psi.PsiClass;
import com.intellij.java.language.psi.PsiModifier;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.psi.PsiElement;
import de.plushnikov.intellij.plugin.LombokClassNames;
import de.plushnikov.intellij.plugin.problem.ProblemSink;
import de.plushnikov.intellij.plugin.processor.clazz.AbstractClassProcessor;
import de.plushnikov.intellij.plugin.processor.handler.SuperBuilderHandler;
import de.plushnikov.intellij.plugin.util.PsiClassUtil;
import jakarta.annotation.Nonnull;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Inspect and validate @SuperBuilder lombok annotation on a class
 * Creates inner classes for a @SuperBuilder pattern
 *
 * @author Michail Plushnikov
 */
@ExtensionImpl
public class SuperBuilderClassProcessor extends AbstractClassProcessor {

  public SuperBuilderClassProcessor() {
    super(PsiClass.class, LombokClassNames.SUPER_BUILDER);
  }

  protected SuperBuilderHandler getBuilderHandler() {
    return new SuperBuilderHandler();
  }

  @Override
  protected Collection<String> getNamesOfPossibleGeneratedElements(@Nonnull PsiClass psiClass, @Nonnull PsiAnnotation psiAnnotation) {
    final SuperBuilderHandler builderHandler = getBuilderHandler();

    final String builderClassName = builderHandler.getBuilderClassName(psiClass);

    if (!psiClass.hasModifierProperty(PsiModifier.ABSTRACT)) {
      final String builderImplClassName = builderHandler.getBuilderImplClassName(psiClass);
      return List.of(builderClassName, builderImplClassName);
    }
    return Collections.singleton(builderClassName);
  }

  @Override
  protected boolean validate(@Nonnull PsiAnnotation psiAnnotation, @Nonnull PsiClass psiClass, @Nonnull ProblemSink builder) {
    return getBuilderHandler().validate(psiClass, psiAnnotation, builder);
  }

  @Override
  protected void generatePsiElements(@Nonnull PsiClass psiClass,
                                     @Nonnull PsiAnnotation psiAnnotation,
                                     @Nonnull List<? super PsiElement> target) {
    SuperBuilderHandler builderHandler = getBuilderHandler();
    final String builderClassName = builderHandler.getBuilderClassName(psiClass);

    Optional<PsiClass> builderClass = PsiClassUtil.getInnerClassInternByName(psiClass, builderClassName);
    if (builderClass.isEmpty()) {
      final PsiClass createdBuilderClass = builderHandler.createBuilderBaseClass(psiClass, psiAnnotation);
      target.add(createdBuilderClass);
      builderClass = Optional.of(createdBuilderClass);
    }

    // skip generation of BuilderImpl class, if class is abstract
    if (!psiClass.hasModifierProperty(PsiModifier.ABSTRACT)) {
      final String builderImplClassName = builderHandler.getBuilderImplClassName(psiClass);
      if (PsiClassUtil.getInnerClassInternByName(psiClass, builderImplClassName).isEmpty()) {
        target.add(builderHandler.createBuilderImplClass(psiClass, builderClass.get(), psiAnnotation));
      }
    }
  }
}
