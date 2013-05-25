package org.consulo.lombok.pg.processors.util;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.psi.PsiElement;
import org.consulo.lombok.module.extension.LombokModuleExtension;
import org.jetbrains.annotations.NotNull;

/**
 * @author VISTALL
 * @since 18:48/25.05.13
 */
public class LombokPgUtil {
  public static boolean isLombokPgExtensionEnabled(@NotNull PsiElement element) {
    Module moduleForPsiElement = ModuleUtilCore.findModuleForPsiElement(element);
    if(moduleForPsiElement == null) {
      return false;
    }
    return ModuleUtil.getExtension(moduleForPsiElement, LombokModuleExtension.class) != null;
  }
}
