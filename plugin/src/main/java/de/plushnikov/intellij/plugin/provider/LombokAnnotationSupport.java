// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package de.plushnikov.intellij.plugin.provider;

import com.intellij.java.language.annoPackages.AnnotationPackageSupport;
import com.intellij.java.language.codeInsight.Nullability;
import consulo.annotation.component.ExtensionImpl;
import jakarta.annotation.Nonnull;

import java.util.Collections;
import java.util.List;

@ExtensionImpl
public class LombokAnnotationSupport implements AnnotationPackageSupport {
  @Nonnull
  @Override
  public List<String> getNullabilityAnnotations(@Nonnull Nullability nullability) {
    if (nullability == Nullability.NOT_NULL) {
      return Collections.singletonList("lombok.NonNull");
    }
    return Collections.emptyList();
  }
}
