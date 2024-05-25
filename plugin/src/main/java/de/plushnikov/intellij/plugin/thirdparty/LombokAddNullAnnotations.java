package de.plushnikov.intellij.plugin.thirdparty;

import com.intellij.java.language.psi.PsiClass;
import consulo.util.lang.StringUtil;
import de.plushnikov.intellij.plugin.lombokconfig.ConfigDiscovery;
import de.plushnikov.intellij.plugin.lombokconfig.LombokNullAnnotationLibrary;
import de.plushnikov.intellij.plugin.psi.LombokLightFieldBuilder;
import de.plushnikov.intellij.plugin.psi.LombokLightMethodBuilder;
import de.plushnikov.intellij.plugin.psi.LombokLightParameter;
import jakarta.annotation.Nonnull;

public final class LombokAddNullAnnotations {
  public static LombokLightMethodBuilder createRelevantNonNullAnnotation(@Nonnull PsiClass psiClass, @Nonnull LombokLightMethodBuilder methodBuilder) {
    final LombokNullAnnotationLibrary annotationLibrary = ConfigDiscovery.getInstance().getAddNullAnnotationLombokConfigProperty(psiClass);
    return createRelevantNonNullAnnotation(annotationLibrary, methodBuilder);
  }

  public static LombokLightMethodBuilder createRelevantNonNullAnnotation(@Nonnull LombokNullAnnotationLibrary annotationLibrary,
                                                                         @Nonnull LombokLightMethodBuilder methodBuilder) {
    if (StringUtil.isNotEmpty(annotationLibrary.getNonNullAnnotation())) {
      methodBuilder.withAnnotation(annotationLibrary.getNonNullAnnotation());
    }
    return methodBuilder;
  }

  public static LombokLightFieldBuilder createRelevantNonNullAnnotation(@Nonnull PsiClass psiClass, @Nonnull LombokLightFieldBuilder fieldBuilder) {
    final LombokNullAnnotationLibrary annotationLibrary = ConfigDiscovery.getInstance().getAddNullAnnotationLombokConfigProperty(psiClass);
    if (StringUtil.isNotEmpty(annotationLibrary.getNonNullAnnotation())) {
      fieldBuilder.withAnnotation(annotationLibrary.getNonNullAnnotation());
    }
    return fieldBuilder;
  }

  public static LombokLightParameter createRelevantNullableAnnotation(@Nonnull PsiClass psiClass,
                                                                      @Nonnull LombokLightParameter lightParameter) {
    final LombokNullAnnotationLibrary annotationLibrary = ConfigDiscovery.getInstance().getAddNullAnnotationLombokConfigProperty(psiClass);
    if (StringUtil.isNotEmpty(annotationLibrary.getNullableAnnotation())) {
      lightParameter.withAnnotation(annotationLibrary.getNullableAnnotation());
    }
    return lightParameter;
  }
}
