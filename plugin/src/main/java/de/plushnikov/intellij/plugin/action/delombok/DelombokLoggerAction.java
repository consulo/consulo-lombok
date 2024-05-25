package de.plushnikov.intellij.plugin.action.delombok;

import consulo.lombok.processor.ProcessorUtil;
import de.plushnikov.intellij.plugin.processor.clazz.log.*;
import jakarta.annotation.Nonnull;

public class DelombokLoggerAction extends AbstractDelombokAction {
  @Override
  @Nonnull
  protected DelombokHandler createHandler() {
    return new DelombokHandler(
      ProcessorUtil.getProcessor(CommonsLogProcessor.class),
      ProcessorUtil.getProcessor(JBossLogProcessor.class),
      ProcessorUtil.getProcessor(Log4jProcessor.class),
      ProcessorUtil.getProcessor(Log4j2Processor.class),
      ProcessorUtil.getProcessor(LogProcessor.class),
      ProcessorUtil.getProcessor(Slf4jProcessor.class),
      ProcessorUtil.getProcessor(XSlf4jProcessor.class),
      ProcessorUtil.getProcessor(FloggerProcessor.class),
      ProcessorUtil.getProcessor(CustomLogProcessor.class));
  }
}
