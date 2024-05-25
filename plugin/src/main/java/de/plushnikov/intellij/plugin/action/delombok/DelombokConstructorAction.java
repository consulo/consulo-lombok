package de.plushnikov.intellij.plugin.action.delombok;

import consulo.lombok.processor.ProcessorUtil;
import de.plushnikov.intellij.plugin.processor.clazz.constructor.AllArgsConstructorProcessor;
import de.plushnikov.intellij.plugin.processor.clazz.constructor.NoArgsConstructorProcessor;
import de.plushnikov.intellij.plugin.processor.clazz.constructor.RequiredArgsConstructorProcessor;
import jakarta.annotation.Nonnull;

public class DelombokConstructorAction extends AbstractDelombokAction {

  @Override
  @Nonnull
  protected DelombokHandler createHandler() {
    return new DelombokHandler(
      ProcessorUtil.getProcessor(AllArgsConstructorProcessor.class),
      ProcessorUtil.getProcessor(NoArgsConstructorProcessor.class),
      ProcessorUtil.getProcessor(RequiredArgsConstructorProcessor.class));
  }

}
