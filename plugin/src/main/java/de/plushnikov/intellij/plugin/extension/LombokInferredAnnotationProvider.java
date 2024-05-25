package de.plushnikov.intellij.plugin.extension;

import com.intellij.java.analysis.impl.codeInspection.dataFlow.JavaMethodContractUtil;
import com.intellij.java.language.codeInsight.InferredAnnotationProvider;
import com.intellij.java.language.psi.PsiAnnotation;
import com.intellij.java.language.psi.PsiModifierListOwner;
import consulo.annotation.component.ExtensionImpl;
import consulo.util.collection.ContainerUtil;
import consulo.util.dataholder.Key;
import de.plushnikov.intellij.plugin.psi.LombokLightMethodBuilder;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.List;

@ExtensionImpl
public class LombokInferredAnnotationProvider implements InferredAnnotationProvider {
  public static final Key<PsiAnnotation> CONTRACT_ANNOTATION = Key.create("lombok.contract");

  @Override
  public @Nullable PsiAnnotation findInferredAnnotation(@Nonnull PsiModifierListOwner listOwner, @Nonnull String annotationFQN) {
    if (!annotationFQN.equals(JavaMethodContractUtil.ORG_JETBRAINS_ANNOTATIONS_CONTRACT)) return null;
    if (!(listOwner instanceof LombokLightMethodBuilder)) return null;
    return listOwner.getUserData(CONTRACT_ANNOTATION);
  }

  @Override
  public @Nonnull List<PsiAnnotation> findInferredAnnotations(@Nonnull PsiModifierListOwner listOwner) {
    return ContainerUtil.createMaybeSingletonList(
      findInferredAnnotation(listOwner, JavaMethodContractUtil.ORG_JETBRAINS_ANNOTATIONS_CONTRACT));
  }
}
