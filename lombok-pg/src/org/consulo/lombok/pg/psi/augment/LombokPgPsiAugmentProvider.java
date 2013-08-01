package org.consulo.lombok.pg.psi.augment;

import org.consulo.lombok.pg.module.extension.LombokPgModuleExtension;
import org.consulo.lombok.psi.augment.LombokPsiAugmentProvider;
import org.consulo.module.extension.ModuleExtension;
import org.jetbrains.annotations.NotNull;

/**
 * @author VISTALL
 * @since 01.08.13.
 */
public class LombokPgPsiAugmentProvider extends LombokPsiAugmentProvider {
  @NotNull
  @Override
  protected Class<? extends ModuleExtension> getModuleExtensionClass() {
    return LombokPgModuleExtension.class;
  }
}
