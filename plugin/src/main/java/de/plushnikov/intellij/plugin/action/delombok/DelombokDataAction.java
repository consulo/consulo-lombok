package de.plushnikov.intellij.plugin.action.delombok;

import consulo.lombok.processor.ProcessorUtil;
import de.plushnikov.intellij.plugin.processor.clazz.DataProcessor;
import jakarta.annotation.Nonnull;

public class DelombokDataAction extends AbstractDelombokAction {

  @Override
  @Nonnull
  protected DelombokHandler createHandler() {
    return new DelombokHandler(ProcessorUtil.getProcessor(DataProcessor.class));
  }
}
