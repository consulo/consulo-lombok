package de.plushnikov.intellij.plugin.psi;


import com.intellij.java.language.JavaLanguage;
import com.intellij.java.language.impl.psi.impl.light.LightParameter;
import com.intellij.java.language.psi.PsiEllipsisType;
import com.intellij.java.language.psi.PsiIdentifier;
import com.intellij.java.language.psi.PsiType;
import consulo.document.util.TextRange;
import consulo.language.Language;
import consulo.language.psi.PsiElement;
import consulo.language.psi.SyntheticElement;
import jakarta.annotation.Nonnull;

import java.util.Objects;
import java.util.stream.Stream;

/**
 * @author Plushnikov Michail
 */
public class LombokLightParameter extends LightParameter implements SyntheticElement {
  private final LombokLightIdentifier myNameIdentifier;

  public LombokLightParameter(@Nonnull String name, @Nonnull PsiType type, @Nonnull PsiElement declarationScope) {
    this(name, type, declarationScope, JavaLanguage.INSTANCE);
  }

  public LombokLightParameter(@Nonnull String name,
                              @Nonnull PsiType type,
                              @Nonnull PsiElement declarationScope,
                              @Nonnull Language language) {
    super(name,
          type,
          declarationScope,
          language,
          new LombokLightModifierList(declarationScope.getManager(), language),
          type instanceof PsiEllipsisType);
    myNameIdentifier = new LombokLightIdentifier(declarationScope.getManager(), name);
  }

  @Nonnull
  @Override
  public String getName() {
    return myNameIdentifier.getText();
  }

  @Override
  public PsiElement setName(@Nonnull String name) {
    myNameIdentifier.setText(name);
    return this;
  }

  @Override
  public PsiIdentifier getNameIdentifier() {
    return myNameIdentifier;
  }

  @Override
  public @Nonnull LombokLightModifierList getModifierList() {
    return (LombokLightModifierList)super.getModifierList();
  }

  public LombokLightParameter withAnnotation(@Nonnull String annotation) {
    getModifierList().addAnnotation(annotation);
    return this;
  }

  @Override
  public TextRange getTextRange() {
    TextRange r = super.getTextRange();
    return r == null ? TextRange.EMPTY_RANGE : r;
  }

  @Override
  public @Nonnull LombokLightParameter setModifiers(@Nonnull String... modifiers) {
    final LombokLightModifierList lombokLightModifierList = getModifierList();
    lombokLightModifierList.clearModifiers();
    Stream.of(modifiers).forEach(lombokLightModifierList::addModifier);
    return this;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    LombokLightParameter that = (LombokLightParameter)o;
    if (!Objects.equals(getName(), that.getName())) {
      return false;
    }

    return getType().equals(that.getType());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getName(), getType());
  }
}
