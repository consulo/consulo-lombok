package de.plushnikov.intellij.plugin.processor.clazz.builder;

import com.intellij.java.language.psi.PsiAnnotation;
import com.intellij.java.language.psi.PsiClass;
import com.intellij.java.language.psi.PsiMethod;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.psi.PsiElement;
import de.plushnikov.intellij.plugin.LombokClassNames;
import de.plushnikov.intellij.plugin.processor.clazz.ToStringProcessor;
import de.plushnikov.intellij.plugin.processor.handler.BuilderHandler;
import de.plushnikov.intellij.plugin.processor.handler.BuilderInfo;
import de.plushnikov.intellij.plugin.util.PsiAnnotationSearchUtil;
import de.plushnikov.intellij.plugin.util.PsiClassUtil;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Creates methods for a builder inner class if it is predefined.
 *
 * @author Michail Plushnikov
 */
@ExtensionImpl
public class BuilderPreDefinedInnerClassMethodProcessor extends AbstractBuilderPreDefinedInnerClassProcessor {

  public BuilderPreDefinedInnerClassMethodProcessor() {
    super(PsiMethod.class, LombokClassNames.BUILDER);
  }


  @Override
  protected Collection<? extends PsiElement> generatePsiElements(@Nonnull PsiClass psiParentClass, @Nullable PsiMethod psiParentMethod, @Nonnull PsiAnnotation psiAnnotation, @Nonnull PsiClass psiBuilderClass) {

    final Collection<String> existedMethodNames = PsiClassUtil.collectClassMethodsIntern(psiBuilderClass).stream()
      .filter(psiMethod -> PsiAnnotationSearchUtil.isNotAnnotatedWith(psiMethod, LombokClassNames.TOLERATE))
      .map(PsiMethod::getName).collect(Collectors.toSet());

    BuilderHandler builderHandler = getBuilderHandler();
    final List<BuilderInfo> builderInfos = builderHandler.createBuilderInfos(psiAnnotation, psiParentClass, psiParentMethod, psiBuilderClass);

    //create constructor
    final Collection<PsiMethod> result = new ArrayList<>(BuilderHandler.createConstructors(psiBuilderClass, psiAnnotation));

    // create builder methods
    builderInfos.stream()
      .filter(info -> info.notAlreadyExistingMethod(existedMethodNames))
      .map(BuilderInfo::renderBuilderMethods)
      .forEach(result::addAll);

    // create 'build' method
    final String buildMethodName = BuilderHandler.getBuildMethodName(psiAnnotation);
    if (!existedMethodNames.contains(buildMethodName)) {
      result.add(builderHandler.createBuildMethod(psiAnnotation, psiParentClass, psiParentMethod, psiBuilderClass, buildMethodName, builderInfos));
    }

    // create 'toString' method
    if (!existedMethodNames.contains(ToStringProcessor.TO_STRING_METHOD_NAME)) {
      result.add(builderHandler.createToStringMethod(psiAnnotation, psiBuilderClass));
    }

    return result;
  }

}
