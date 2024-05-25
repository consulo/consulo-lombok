package de.plushnikov.intellij.plugin.processor.clazz.log;

import com.intellij.java.language.psi.PsiClass;
import jakarta.annotation.Nonnull;

import java.util.Collections;
import java.util.List;

abstract class AbstractSimpleLogProcessor extends AbstractLogProcessor {
  @Nonnull
  private final String loggerType;
  @Nonnull
  private final String loggerInitializer;

  AbstractSimpleLogProcessor(
    @Nonnull String supportedAnnotationClass,
    @Nonnull String loggerType,
    @Nonnull String loggerInitializer
  ) {
    super(supportedAnnotationClass);
    this.loggerType = loggerType;
    this.loggerInitializer = loggerInitializer;
  }

  @Nonnull
  @Override
  public final String getLoggerType(@Nonnull PsiClass psiClass) {
    return loggerType;
  }

  @Nonnull
  @Override
  final String getLoggerInitializer(@Nonnull PsiClass psiClass) {
    return loggerInitializer;
  }
}

abstract class AbstractTopicSupportingSimpleLogProcessor extends AbstractSimpleLogProcessor {
  @Nonnull
  private final LoggerInitializerParameter defaultParameter;

  AbstractTopicSupportingSimpleLogProcessor(
    @Nonnull String supportedAnnotationClass,
    @Nonnull String loggerType,
    @Nonnull String loggerInitializer,
    @Nonnull LoggerInitializerParameter defaultParameter
  ) {
    super(supportedAnnotationClass, loggerType, loggerInitializer);
    this.defaultParameter = defaultParameter;
  }

  @Nonnull
  @Override
  final List<LoggerInitializerParameter> getLoggerInitializerParameters(@Nonnull PsiClass psiClass, boolean topicPresent) {
    return Collections.singletonList(topicPresent ? LoggerInitializerParameter.TOPIC : defaultParameter);
  }
}
