package de.plushnikov.intellij.plugin.action.delombok;

import consulo.lombok.processor.ProcessorUtil;
import de.plushnikov.intellij.plugin.processor.field.DelegateFieldProcessor;
import de.plushnikov.intellij.plugin.processor.method.DelegateMethodProcessor;
import jakarta.annotation.Nonnull;

public class DelombokDelegateAction extends AbstractDelombokAction {

  @Override
  @Nonnull
  protected DelombokHandler createHandler() {
    return new DelombokHandler(
      ProcessorUtil.getProcessor(DelegateFieldProcessor.class),
      ProcessorUtil.getProcessor(DelegateMethodProcessor.class));
  }
}
