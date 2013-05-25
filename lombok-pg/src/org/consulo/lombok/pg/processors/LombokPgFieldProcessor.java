package org.consulo.lombok.pg.processors;

import org.consulo.lombok.pg.module.extension.LombokPgModuleExtension;
import org.consulo.lombok.processors.LombokFieldProcessor;
import org.consulo.module.extension.ModuleExtension;

/**
 * @author VISTALL
 * @since 18:56/25.05.13
 */
public abstract class LombokPgFieldProcessor extends LombokFieldProcessor {
  public LombokPgFieldProcessor(String annotationClass) {
    super(annotationClass);
  }

  @Override
  public Class<? extends ModuleExtension> getModuleExtensionClass() {
    return LombokPgModuleExtension.class;
  }
}
