package de.plushnikov.intellij.plugin.action.delombok;

import consulo.lombok.processor.ProcessorUtil;
import de.plushnikov.intellij.plugin.processor.clazz.GetterProcessor;
import de.plushnikov.intellij.plugin.processor.field.GetterFieldProcessor;
import jakarta.annotation.Nonnull;

public class DelombokGetterAction extends AbstractDelombokAction {

  @Override
  @Nonnull
  protected DelombokHandler createHandler() {
    return new DelombokHandler(
      ProcessorUtil.getProcessor(GetterProcessor.class),
      ProcessorUtil.getProcessor(GetterFieldProcessor.class));
  }
}
