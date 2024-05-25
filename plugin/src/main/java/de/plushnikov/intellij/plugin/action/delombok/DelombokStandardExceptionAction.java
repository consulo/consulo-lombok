package de.plushnikov.intellij.plugin.action.delombok;

import consulo.lombok.processor.ProcessorUtil;
import de.plushnikov.intellij.plugin.processor.clazz.StandardExceptionProcessor;
import jakarta.annotation.Nonnull;

public class DelombokStandardExceptionAction extends AbstractDelombokAction {
  @Override
  @Nonnull
  protected DelombokHandler createHandler() {
    return new DelombokHandler(true, ProcessorUtil.getProcessor(StandardExceptionProcessor.class));
  }
}
