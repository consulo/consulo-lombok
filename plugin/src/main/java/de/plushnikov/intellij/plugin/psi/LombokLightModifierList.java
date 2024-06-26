package de.plushnikov.intellij.plugin.psi;

import com.intellij.java.language.JavaLanguage;
import com.intellij.java.language.impl.psi.impl.light.LightModifierList;
import com.intellij.java.language.psi.JavaPsiFacade;
import com.intellij.java.language.psi.PsiAnnotation;
import com.intellij.java.language.psi.PsiElementFactory;
import com.intellij.java.language.psi.PsiModifier;
import consulo.document.util.TextRange;
import consulo.language.Language;
import consulo.language.psi.PsiManager;
import consulo.language.psi.SyntheticElement;
import consulo.language.util.IncorrectOperationException;
import jakarta.annotation.Nonnull;
import org.jetbrains.annotations.NonNls;

import java.util.*;

/**
 * @author Plushnikov Michail
 */
public class LombokLightModifierList extends LightModifierList implements SyntheticElement {

  private final Map<String, PsiAnnotation> myAnnotations;
  private final Set<String> myImplicitModifiers;

  public LombokLightModifierList(@Nonnull PsiManager manager) {
    this(manager, JavaLanguage.INSTANCE);
  }

  public LombokLightModifierList(@Nonnull PsiManager manager, @Nonnull Language language) {
    this(manager, language, Collections.emptyList());
  }

  public LombokLightModifierList(PsiManager manager, final Language language, Collection<String> implicitModifiers, String... modifiers) {
    super(manager, language, modifiers);
    this.myAnnotations = new HashMap<>();
    this.myImplicitModifiers = new HashSet<>(implicitModifiers);
  }

  @Override
  public boolean hasModifierProperty(@Nonnull String name) {
    return myImplicitModifiers.contains(name) || super.hasModifierProperty(name);
  }

  public void addImplicitModifierProperty(@PsiModifier.ModifierConstant @Nonnull @NonNls String implicitModifier) {
    myImplicitModifiers.add(implicitModifier);
  }

  @Override
  public void setModifierProperty(@PsiModifier.ModifierConstant @Nonnull @NonNls String name, boolean value) throws IncorrectOperationException {
    if (value) {
      addModifier(name);
    } else {
      if (hasModifierProperty(name)) {
        removeModifier(name);
      }
    }
  }

  private void removeModifier(@PsiModifier.ModifierConstant @Nonnull @NonNls String name) {
    final Collection<String> myModifiers = collectAllModifiers();
    myModifiers.remove(name);

    clearModifiers();

    for (String modifier : myModifiers) {
      addModifier(modifier);
    }
  }

  private Collection<String> collectAllModifiers() {
    Collection<String> result = new HashSet<>();
    for (@PsiModifier.ModifierConstant String modifier : PsiModifier.MODIFIERS) {
      if (hasExplicitModifier(modifier)) {
        result.add(modifier);
      }
    }
    return result;
  }

  @Override
  public void checkSetModifierProperty(@PsiModifier.ModifierConstant @Nonnull @NonNls String name, boolean value) throws IncorrectOperationException {
    throw new IncorrectOperationException();
  }

  public LombokLightModifierList withAnnotation(@Nonnull PsiAnnotation psiAnnotation) {
    myAnnotations.put(psiAnnotation.getQualifiedName(), psiAnnotation);
    return this;
  }

  @Override
  @Nonnull
  public PsiAnnotation addAnnotation(@Nonnull @NonNls String qualifiedName) {
    final PsiElementFactory elementFactory = JavaPsiFacade.getElementFactory(getProject());
    final PsiAnnotation psiAnnotation = elementFactory.createAnnotationFromText('@' + qualifiedName, null);
    myAnnotations.put(qualifiedName, psiAnnotation);
    return psiAnnotation;
  }

  @Override
  public PsiAnnotation findAnnotation(@Nonnull String qualifiedName) {
    return myAnnotations.get(qualifiedName);
  }

  @Override
  @Nonnull
  public PsiAnnotation[] getAnnotations() {
    PsiAnnotation[] result = PsiAnnotation.EMPTY_ARRAY;
    if (!myAnnotations.isEmpty()) {
      Collection<PsiAnnotation> annotations = myAnnotations.values();
      result = annotations.toArray(PsiAnnotation.EMPTY_ARRAY);
    }
    return result;
  }

  @Override
  public TextRange getTextRange() {
    TextRange r = super.getTextRange();
    return r == null ? TextRange.EMPTY_RANGE : r;
  }

  public String toString() {
    return "LombokLightModifierList";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    LombokLightModifierList that = (LombokLightModifierList) o;

    return myAnnotations.equals(that.myAnnotations);
  }

  @Override
  public int hashCode() {
    return myAnnotations.hashCode();
  }
}
