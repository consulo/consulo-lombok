package de.plushnikov.intellij.plugin.psi;

import com.intellij.java.language.impl.psi.impl.light.LightReferenceListBuilder;
import consulo.document.util.TextRange;
import consulo.language.Language;
import consulo.language.psi.PsiManager;
import consulo.language.psi.SyntheticElement;

public class LombokLightReferenceListBuilder extends LightReferenceListBuilder implements SyntheticElement {

  public LombokLightReferenceListBuilder(PsiManager manager, Language language, Role role) {
    super(manager, language, role);
  }

  @Override
  public TextRange getTextRange() {
    TextRange r = super.getTextRange();
    return r == null ? TextRange.EMPTY_RANGE : r;
  }
}
