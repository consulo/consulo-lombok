package de.plushnikov.intellij.plugin.provider;

import com.intellij.java.impl.codeInspection.DefaultAnnotationParamIgnoreFilter;
import consulo.annotation.component.ExtensionImpl;
import de.plushnikov.intellij.plugin.LombokClassNames;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * Ignore DefaultAnnotationParamInspection for lombok EqualsAndHashCode annotation and callSuper param
 */
@ExtensionImpl
public class LombokDefaultAnnotationParamSupport implements DefaultAnnotationParamIgnoreFilter {

  @Override
  public boolean ignoreAnnotationParam(@Nullable String annotationFQN, @Nonnull String annotationParameterName) {
    return LombokClassNames.ACCESSORS.equals(annotationFQN) ||
           LombokClassNames.EQUALS_AND_HASHCODE.equals(annotationFQN) && ("callSuper".equals(annotationParameterName) || "of".equals(annotationParameterName));
  }
}
