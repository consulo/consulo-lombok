package de.plushnikov.intellij.plugin.processor.modifier;

import com.intellij.java.language.psi.PsiModifierList;
import jakarta.annotation.Nonnull;

import java.util.Set;

/**
 * To support augmentation of {@link PsiModifierList} properties, processors should implement this interface.
 *
 * @author Alexej Kubarev
 * @see PsiAugmentProvider#transformModifiers(PsiModifierList, Set)
 */
public interface ModifierProcessor {

  /**
   * Validates if this {@link ModifierProcessor} implementation supports provided property on a {@link PsiModifierList}.
   * This method <strong>should not</strong> do heavy computations and defer them to {@link #transformModifiers(PsiModifierList, Set)} instead.
   *
   * @param modifierList Modifier List that will have modifiers augmented
   * @return true if supported and therefore may be passed to {@link #transformModifiers(PsiModifierList, Set)}, false otherwise
   */
  boolean isSupported(@Nonnull PsiModifierList modifierList);

  /**
   * Compute modification of  response for {@link PsiAugmentProvider#transformModifiers(PsiModifierList, Set)}.
   *
   * @param modifierList Modifier List that will have mosifiers augmented
   * @param modifiers    Set of modifiers that is currently present for the list
   */
  void transformModifiers(@Nonnull PsiModifierList modifierList, @Nonnull final Set<String> modifiers);
}
