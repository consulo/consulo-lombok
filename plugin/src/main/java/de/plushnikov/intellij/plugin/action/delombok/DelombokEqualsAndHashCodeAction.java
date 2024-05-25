package de.plushnikov.intellij.plugin.action.delombok;

import consulo.lombok.processor.ProcessorUtil;
import de.plushnikov.intellij.plugin.processor.clazz.EqualsAndHashCodeProcessor;

public class DelombokEqualsAndHashCodeAction extends AbstractDelombokAction {

  @Override
  protected DelombokHandler createHandler() {
    return new DelombokHandler(ProcessorUtil.getProcessor(EqualsAndHashCodeProcessor.class));
  }
}
