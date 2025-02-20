package de.plushnikov.intellij.plugin.processor.clazz.builder;

import com.intellij.java.language.psi.PsiAnnotation;
import com.intellij.java.language.psi.PsiClass;
import com.intellij.java.language.psi.PsiField;
import com.intellij.java.language.psi.PsiMethod;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.psi.PsiElement;
import de.plushnikov.intellij.plugin.LombokClassNames;
import de.plushnikov.intellij.plugin.processor.handler.BuilderInfo;
import de.plushnikov.intellij.plugin.util.PsiClassUtil;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Creates fields for a @Builder inner class if it is predefined.
 *
 * @author Michail Plushnikov
 */
@ExtensionImpl(id = "BuilderPreDefinedInnerClassFieldProcessor", order = "after WitherProcessor")
public class BuilderPreDefinedInnerClassFieldProcessor extends AbstractBuilderPreDefinedInnerClassProcessor {

  public BuilderPreDefinedInnerClassFieldProcessor() {
    super(PsiField.class, LombokClassNames.BUILDER);
  }

  @Override
  protected Collection<? extends PsiElement> generatePsiElements(@Nonnull PsiClass psiParentClass, @Nullable PsiMethod psiParentMethod, @Nonnull PsiAnnotation psiAnnotation, @Nonnull PsiClass psiBuilderClass) {
    final Collection<String> existedFieldNames = PsiClassUtil.collectClassFieldsIntern(psiBuilderClass).stream()
      .map(PsiField::getName)
      .collect(Collectors.toSet());

    final List<BuilderInfo> builderInfos = getBuilderHandler().createBuilderInfos(psiAnnotation, psiParentClass, psiParentMethod, psiBuilderClass);
    return builderInfos.stream()
      .filter(info -> info.notAlreadyExistingField(existedFieldNames))
      .map(BuilderInfo::renderBuilderFields)
      .flatMap(Collection::stream)
      .collect(Collectors.toList());
  }
}
