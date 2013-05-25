package org.consulo.lombok.pg.processors;

import org.consulo.lombok.pg.module.extension.LombokPgModuleExtension;
import org.consulo.lombok.processors.LombokSelfClassProcessor;
import org.consulo.module.extension.ModuleExtension;

/**
 * @author VISTALL
 * @since 18:56/25.05.13
 */
public abstract class LombokPgSelfClassProcessor extends LombokSelfClassProcessor {
  public LombokPgSelfClassProcessor(String annotationClass) {
    super(annotationClass);
  }

  @Override
  public Class<? extends ModuleExtension> getModuleExtensionClass() {
    return LombokPgModuleExtension.class;
  }
}
