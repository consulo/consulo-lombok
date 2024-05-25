package de.plushnikov.intellij.plugin.processor.handler.singular;

import com.intellij.java.language.psi.PsiAnnotation;
import com.intellij.java.language.psi.PsiField;
import com.intellij.java.language.psi.PsiMethod;
import de.plushnikov.intellij.plugin.processor.handler.BuilderInfo;
import de.plushnikov.intellij.plugin.thirdparty.CapitalizationStrategy;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

class EmptyBuilderElementHandler implements BuilderElementHandler {

  @Override
  public Collection<PsiField> renderBuilderFields(@Nonnull BuilderInfo info) {
    return Collections.emptyList();
  }

  @Override
  public Collection<PsiMethod> renderBuilderMethod(@Nonnull BuilderInfo info) {
    return Collections.emptyList();
  }

  @Override
  public String calcBuilderMethodName(@Nonnull BuilderInfo info) {
    return "";
  }

  @Override
  public List<String> getBuilderMethodNames(@Nonnull String fieldName, @Nonnull String prefix,
                                            @Nullable PsiAnnotation singularAnnotation, CapitalizationStrategy capitalizationStrategy) {
    return Collections.emptyList();
  }

  @Override
  public String createSingularName(PsiAnnotation singularAnnotation, String psiFieldName) {
    return psiFieldName;
  }

}
