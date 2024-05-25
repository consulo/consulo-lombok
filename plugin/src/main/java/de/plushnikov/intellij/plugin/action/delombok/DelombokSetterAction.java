package de.plushnikov.intellij.plugin.action.delombok;

import consulo.lombok.processor.ProcessorUtil;
import de.plushnikov.intellij.plugin.processor.clazz.SetterProcessor;
import de.plushnikov.intellij.plugin.processor.field.SetterFieldProcessor;
import jakarta.annotation.Nonnull;

public class DelombokSetterAction extends AbstractDelombokAction {
  @Override
  @Nonnull
  protected DelombokHandler createHandler() {
    return new DelombokHandler(
      ProcessorUtil.getProcessor(SetterProcessor.class),
      ProcessorUtil.getProcessor(SetterFieldProcessor.class));
  }
}
