package de.plushnikov.intellij.plugin.action.delombok;

import consulo.lombok.processor.ProcessorUtil;
import de.plushnikov.intellij.plugin.processor.clazz.UtilityClassProcessor;
import jakarta.annotation.Nonnull;

public class DelombokUtilityClassAction extends AbstractDelombokAction {
  @Override
  @Nonnull
  protected DelombokHandler createHandler() {
    return new DelombokHandler(true, ProcessorUtil.getProcessor(UtilityClassProcessor.class));
  }
}
