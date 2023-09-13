package de.plushnikov.intellij.plugin.extension;

import com.intellij.java.analysis.impl.codeInspection.canBeFinal.CanBeFinalHandler;
import com.intellij.java.language.psi.PsiClass;
import com.intellij.java.language.psi.PsiField;
import com.intellij.java.language.psi.PsiMember;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.psi.util.PsiTreeUtil;
import de.plushnikov.intellij.plugin.LombokClassNames;
import de.plushnikov.intellij.plugin.util.PsiAnnotationSearchUtil;
import org.jetbrains.annotations.NotNull;

/**
 * Handler to produce a veto for elements with lombok methods behind
 */
@ExtensionImpl
public class LombokCanBeFinalHandler extends CanBeFinalHandler {

  @Override
  public boolean canBeFinal(@NotNull PsiMember member) {
    if (member instanceof PsiField) {
      if (PsiAnnotationSearchUtil.isAnnotatedWith(member, LombokClassNames.SETTER)) {
        return false;
      }

      final PsiClass psiClass = PsiTreeUtil.getParentOfType(member, PsiClass.class);
      return null == psiClass || !PsiAnnotationSearchUtil.isAnnotatedWith(psiClass, LombokClassNames.SETTER, LombokClassNames.DATA, LombokClassNames.VALUE);
    }
    return true;
  }
}
