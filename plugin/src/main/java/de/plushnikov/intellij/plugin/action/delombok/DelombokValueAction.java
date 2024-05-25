package de.plushnikov.intellij.plugin.action.delombok;

import consulo.lombok.processor.ProcessorUtil;
import de.plushnikov.intellij.plugin.processor.clazz.ValueProcessor;
import jakarta.annotation.Nonnull;

public class DelombokValueAction extends AbstractDelombokAction {
  @Override
  @Nonnull
  protected DelombokHandler createHandler() {
    return new DelombokHandler(ProcessorUtil.getProcessor(ValueProcessor.class));
  }
}
