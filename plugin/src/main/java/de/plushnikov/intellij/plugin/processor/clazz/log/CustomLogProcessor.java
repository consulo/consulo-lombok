package de.plushnikov.intellij.plugin.processor.clazz.log;

import com.intellij.java.language.psi.PsiAnnotation;
import com.intellij.java.language.psi.PsiClass;
import consulo.annotation.component.ExtensionImpl;
import consulo.util.lang.StringUtil;
import de.plushnikov.intellij.plugin.LombokClassNames;
import de.plushnikov.intellij.plugin.lombokconfig.ConfigDiscovery;
import de.plushnikov.intellij.plugin.lombokconfig.ConfigKey;
import de.plushnikov.intellij.plugin.problem.ProblemSink;
import de.plushnikov.intellij.plugin.processor.clazz.log.CustomLogParser.LoggerInitializerDeclaration;
import de.plushnikov.intellij.plugin.util.PsiAnnotationUtil;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.List;

/**
 * @author Adam Juraszek
 */
@ExtensionImpl
public class CustomLogProcessor extends AbstractLogProcessor {

  public CustomLogProcessor() {
    super(LombokClassNames.CUSTOM_LOG);
  }

  @Nonnull
  private static String getCustomDeclaration(@Nonnull PsiClass psiClass) {
    return ConfigDiscovery.getInstance().getStringLombokConfigProperty(ConfigKey.LOG_CUSTOM_DECLARATION, psiClass);
  }

  @Nullable
  @Override
  public String getLoggerType(@Nonnull PsiClass psiClass) {
    return CustomLogParser.parseLoggerType(getCustomDeclaration(psiClass));
  }

  @Nonnull
  @Override
  String getLoggerInitializer(@Nonnull PsiClass psiClass) {
    String loggerInitializer = CustomLogParser.parseLoggerInitializer(getCustomDeclaration(psiClass));
    if (loggerInitializer == null) {
      throw new IllegalStateException("Invalid custom log declaration."); // validated
    }
    return loggerInitializer;
  }

  @Nonnull
  @Override
  List<LoggerInitializerParameter> getLoggerInitializerParameters(@Nonnull PsiClass psiClass, boolean topicPresent) {
    LoggerInitializerDeclaration declaration = CustomLogParser.parseInitializerParameters(getCustomDeclaration(psiClass));
    if (declaration == null) {
      throw new IllegalStateException("Invalid custom log declaration."); // validated
    }

    if (!declaration.has(topicPresent)) {
      throw new IllegalStateException("@CustomLog is not configured to work " + (topicPresent ? "with" : "without") + " topic.");
    }
    return declaration.get(topicPresent);
  }

  @Override
  protected boolean validate(
    @Nonnull PsiAnnotation psiAnnotation, @Nonnull PsiClass psiClass, @Nonnull ProblemSink builder
  ) {
    if (!super.validate(psiAnnotation, psiClass, builder)) {
      return false;
    }

    final LoggerInitializerDeclaration declaration = CustomLogParser.parseInitializerParameters(getCustomDeclaration(psiClass));
    if (declaration == null) {
      builder.addErrorMessage("inspection.message.custom.log.not.configured.correctly");
      return false;
    }
    final String topic = PsiAnnotationUtil.getStringAnnotationValue(psiAnnotation, "topic", "");
    final boolean topicPresent = !StringUtil.isEmptyOrSpaces(topic);
    if (topicPresent) {
      if (!declaration.hasWithTopic()) {
        builder.addErrorMessage("inspection.message.custom.log.does.not.allow.topic");
        return false;
      }
    } else {
      if (!declaration.hasWithoutTopic()) {
        builder.addErrorMessage("inspection.message.custom.log.requires.topic");
        return false;
      }
    }
    return true;
  }

}
