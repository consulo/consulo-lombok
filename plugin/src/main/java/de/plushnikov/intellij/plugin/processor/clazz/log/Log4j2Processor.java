package de.plushnikov.intellij.plugin.processor.clazz.log;

import consulo.annotation.component.ExtensionImpl;
import de.plushnikov.intellij.plugin.LombokClassNames;

/**
 * @author Plushnikov Michail
 */
@ExtensionImpl(id = "Log4j2Processor", order = "after Log4jProcessor")
public class Log4j2Processor extends AbstractTopicSupportingSimpleLogProcessor {

  private static final String LOGGER_TYPE = "org.apache.logging.log4j.Logger";
  private static final String LOGGER_INITIALIZER = "org.apache.logging.log4j.LogManager.getLogger(%s)";

  public Log4j2Processor() {
    super(LombokClassNames.LOG_4_J_2, LOGGER_TYPE, LOGGER_INITIALIZER, LoggerInitializerParameter.TYPE);
  }
}
