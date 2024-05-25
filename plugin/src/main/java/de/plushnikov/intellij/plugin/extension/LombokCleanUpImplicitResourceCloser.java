package de.plushnikov.intellij.plugin.extension;

import com.intellij.java.language.psi.PsiVariable;
import consulo.annotation.component.ExtensionImpl;
import consulo.java.analysis.codeInspection.ImplicitResourceCloser;
import de.plushnikov.intellij.plugin.LombokClassNames;
import de.plushnikov.intellij.plugin.util.PsiAnnotationSearchUtil;
import jakarta.annotation.Nonnull;

/**
 * Implement additional way to close AutoCloseables by @lombok.Cleanup for IntelliJ
 */
@ExtensionImpl
public class LombokCleanUpImplicitResourceCloser implements ImplicitResourceCloser {

  @Override
  public boolean isSafelyClosed(@Nonnull PsiVariable variable) {
    return PsiAnnotationSearchUtil.isAnnotatedWith(variable, LombokClassNames.CLEANUP);
  }
}
