package de.plushnikov.intellij.plugin.processor.clazz.log;

import consulo.annotation.component.ExtensionImpl;
import de.plushnikov.intellij.plugin.LombokClassNames;

/**
 * @author Plushnikov Michail
 */
@ExtensionImpl(id = "XSlf4jProcessor", order = "after Slf4jProcessor")
public class XSlf4jProcessor extends AbstractTopicSupportingSimpleLogProcessor {

  private static final String LOGGER_TYPE = "org.slf4j.ext.XLogger";
  private static final String LOGGER_INITIALIZER = "org.slf4j.ext.XLoggerFactory.getXLogger(%s)";

  public XSlf4jProcessor() {
    super(LombokClassNames.XSLF_4_J, LOGGER_TYPE, LOGGER_INITIALIZER, LoggerInitializerParameter.TYPE);
  }
}
