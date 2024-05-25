package de.plushnikov.intellij.plugin.action.delombok;

import consulo.lombok.processor.ProcessorUtil;
import de.plushnikov.intellij.plugin.processor.clazz.WitherProcessor;
import de.plushnikov.intellij.plugin.processor.field.WitherFieldProcessor;
import jakarta.annotation.Nonnull;

public class DelombokWitherAction extends AbstractDelombokAction {
  @Override
  @Nonnull
  protected DelombokHandler createHandler() {
    return new DelombokHandler(
      ProcessorUtil.getProcessor(WitherProcessor.class),
      ProcessorUtil.getProcessor(WitherFieldProcessor.class));
  }
}
