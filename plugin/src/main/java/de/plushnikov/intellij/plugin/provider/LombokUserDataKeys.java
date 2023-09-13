package de.plushnikov.intellij.plugin.provider;

import consulo.util.dataholder.Key;

import java.util.Collection;

public final class LombokUserDataKeys {
  public static final Key<Collection<String>> AUGMENTED_ANNOTATIONS = Key.create("lombok.augmented.annotations");
}
