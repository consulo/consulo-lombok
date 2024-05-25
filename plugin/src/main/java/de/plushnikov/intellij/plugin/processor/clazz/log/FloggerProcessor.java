package de.plushnikov.intellij.plugin.processor.clazz.log;

import com.intellij.java.language.psi.PsiClass;
import consulo.annotation.component.ExtensionImpl;
import de.plushnikov.intellij.plugin.LombokClassNames;
import jakarta.annotation.Nonnull;

import java.util.Collections;
import java.util.List;

@ExtensionImpl
public class FloggerProcessor extends AbstractSimpleLogProcessor {
  private static final String LOGGER_TYPE = "com.google.common.flogger.FluentLogger";
  private static final String LOGGER_INITIALIZER = "com.google.common.flogger.FluentLogger.forEnclosingClass()";

  public FloggerProcessor() {
    super(LombokClassNames.FLOGGER, LOGGER_TYPE, LOGGER_INITIALIZER);
  }

  @Nonnull
  @Override
  List<LoggerInitializerParameter> getLoggerInitializerParameters(@Nonnull PsiClass psiClass, boolean topicPresent) {
    if (topicPresent) {
      throw new IllegalStateException("Flogger does not allow to set a topic.");
    }
    return Collections.emptyList();
  }
}
