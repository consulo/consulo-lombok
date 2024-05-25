package de.plushnikov.intellij.plugin.processor.modifier;

import com.intellij.java.language.psi.PsiLocalVariable;
import com.intellij.java.language.psi.PsiModifier;
import com.intellij.java.language.psi.PsiModifierList;
import consulo.language.psi.PsiElement;
import de.plushnikov.intellij.plugin.processor.ValProcessor;
import jakarta.annotation.Nonnull;

import java.util.Set;

/**
 * @author Alexej Kubarev
 */
public class ValModifierProcessor implements ModifierProcessor {

  @Override
  public boolean isSupported(@Nonnull PsiModifierList modifierList) {
    final PsiElement parent = modifierList.getParent();

    return (parent instanceof PsiLocalVariable && ValProcessor.isVal((PsiLocalVariable) parent));
  }

  @Override
  public void transformModifiers(@Nonnull PsiModifierList modifierList, @Nonnull final Set<String> modifiers) {
    modifiers.add(PsiModifier.FINAL);
  }
}
