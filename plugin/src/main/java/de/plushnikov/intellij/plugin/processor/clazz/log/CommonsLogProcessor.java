package de.plushnikov.intellij.plugin.processor.clazz.log;

import consulo.annotation.component.ExtensionImpl;
import de.plushnikov.intellij.plugin.LombokClassNames;

/**
 * @author Plushnikov Michail
 */
@ExtensionImpl(id = "CommonsLogProcessor", order = "after XSlf4jProcessor")
public class CommonsLogProcessor extends AbstractTopicSupportingSimpleLogProcessor {

  private static final String LOGGER_TYPE = "org.apache.commons.logging.Log";
  private static final String LOGGER_INITIALIZER = "org.apache.commons.logging.LogFactory.getLog(%s)";

  public CommonsLogProcessor() {
    super(LombokClassNames.COMMONS_LOG, LOGGER_TYPE, LOGGER_INITIALIZER, LoggerInitializerParameter.TYPE);
  }
}
