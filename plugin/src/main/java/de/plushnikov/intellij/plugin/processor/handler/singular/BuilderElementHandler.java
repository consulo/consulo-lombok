package de.plushnikov.intellij.plugin.processor.handler.singular;

import com.intellij.java.language.psi.PsiAnnotation;
import com.intellij.java.language.psi.PsiField;
import com.intellij.java.language.psi.PsiMethod;
import com.intellij.java.language.psi.PsiVariable;
import de.plushnikov.intellij.plugin.processor.handler.BuilderInfo;
import de.plushnikov.intellij.plugin.thirdparty.CapitalizationStrategy;
import de.plushnikov.intellij.plugin.thirdparty.LombokUtils;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.Collection;
import java.util.List;

public interface BuilderElementHandler {

  String createSingularName(PsiAnnotation singularAnnotation, String psiFieldName);

  default String renderBuildPrepare(@Nonnull BuilderInfo info) {
    return "";
  }

  default String renderBuildCall(@Nonnull BuilderInfo info) {
    return "this." + info.renderFieldName();
  }

  default String renderSuperBuilderConstruction(@Nonnull PsiVariable psiVariable, @Nonnull String fieldName) {
    return "this." + psiVariable.getName() + "=b." + fieldName + ";\n";
  }

  default String renderToBuilderCall(@Nonnull BuilderInfo info) {
    return calcBuilderMethodName(info) + '(' + info.getInstanceVariableName() + '.' + info.getVariable().getName() + ')';
  }

  default String renderToBuilderAppendCall(@Nonnull BuilderInfo info) {
    return "";
  }

  Collection<PsiField> renderBuilderFields(@Nonnull BuilderInfo info);

  default String calcBuilderMethodName(@Nonnull BuilderInfo info) {
    return LombokUtils.buildAccessorName(info.getSetterPrefix(), info.getFieldName(), info.getCapitalizationStrategy());
  }

  Collection<PsiMethod> renderBuilderMethod(@Nonnull BuilderInfo info);

  List<String> getBuilderMethodNames(@Nonnull String fieldName, @Nonnull String prefix,
                                     @Nullable PsiAnnotation singularAnnotation, CapitalizationStrategy capitalizationStrategy);
}
