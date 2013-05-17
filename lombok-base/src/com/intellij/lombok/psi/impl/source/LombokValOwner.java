package com.intellij.lombok.psi.impl.source;

import com.intellij.lombok.psi.LombokElementWithAdditionalModifiers;
import com.intellij.psi.PsiType;
import org.jetbrains.annotations.Nullable;

/**
 * @author VISTALL
 * @since 20:30/02.05.13
 */
public interface LombokValOwner extends LombokElementWithAdditionalModifiers {
  @Nullable
  PsiType findRightTypeIfCan();
}
