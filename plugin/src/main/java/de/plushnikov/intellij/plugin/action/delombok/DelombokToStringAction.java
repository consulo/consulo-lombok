package de.plushnikov.intellij.plugin.action.delombok;

import consulo.lombok.processor.ProcessorUtil;
import de.plushnikov.intellij.plugin.processor.clazz.ToStringProcessor;
import jakarta.annotation.Nonnull;

public class DelombokToStringAction extends AbstractDelombokAction {
  @Override
  @Nonnull
  protected DelombokHandler createHandler() {
    return new DelombokHandler(ProcessorUtil.getProcessor(ToStringProcessor.class));
  }
}
