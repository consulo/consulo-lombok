package de.plushnikov.intellij.plugin.processor.clazz.builder;

import com.intellij.java.language.psi.PsiAnnotation;
import com.intellij.java.language.psi.PsiClass;
import com.intellij.java.language.psi.PsiField;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.psi.PsiElement;
import de.plushnikov.intellij.plugin.LombokClassNames;
import de.plushnikov.intellij.plugin.processor.handler.BuilderInfo;
import de.plushnikov.intellij.plugin.util.PsiClassUtil;
import jakarta.annotation.Nonnull;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Creates fields for a @SuperBuilder inner class if it is predefined.
 *
 * @author Michail Plushnikov
 */
@ExtensionImpl(id = "SuperBuilderPreDefinedInnerClassFieldProcessor", order = "after BuilderMethodProcessor")
public class SuperBuilderPreDefinedInnerClassFieldProcessor extends AbstractSuperBuilderPreDefinedInnerClassProcessor {

  public SuperBuilderPreDefinedInnerClassFieldProcessor() {
    super(PsiField.class, LombokClassNames.SUPER_BUILDER);
  }

  @Override
  protected Collection<? extends PsiElement> generatePsiElementsOfBaseBuilderClass(@Nonnull PsiClass psiParentClass, @Nonnull PsiAnnotation psiAnnotation, @Nonnull PsiClass psiBuilderClass) {
    final Collection<String> existedFieldNames = PsiClassUtil.collectClassFieldsIntern(psiBuilderClass).stream()
      .map(PsiField::getName)
      .collect(Collectors.toSet());

    final List<BuilderInfo> builderInfos = getBuilderHandler().createBuilderInfos(psiAnnotation, psiParentClass, null, psiBuilderClass);
    return builderInfos.stream()
      .filter(info -> info.notAlreadyExistingField(existedFieldNames))
      .map(BuilderInfo::renderBuilderFields)
      .flatMap(Collection::stream)
      .collect(Collectors.toList());
  }

  @Override
  protected Collection<? extends PsiElement> generatePsiElementsOfImplBuilderClass(@Nonnull PsiClass psiParentClass, @Nonnull PsiAnnotation psiAnnotation, @Nonnull PsiClass psiBuilderClass) {
    // ImplBuilder doesn't contains any fields
    return Collections.emptyList();
  }
}
