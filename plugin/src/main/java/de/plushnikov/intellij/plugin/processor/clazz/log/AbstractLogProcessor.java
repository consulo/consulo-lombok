package de.plushnikov.intellij.plugin.processor.clazz.log;

import com.intellij.java.language.psi.*;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiManager;
import consulo.project.Project;
import consulo.util.lang.StringUtil;
import de.plushnikov.intellij.plugin.lombokconfig.ConfigDiscovery;
import de.plushnikov.intellij.plugin.lombokconfig.ConfigKey;
import de.plushnikov.intellij.plugin.problem.ProblemSink;
import de.plushnikov.intellij.plugin.processor.clazz.AbstractClassProcessor;
import de.plushnikov.intellij.plugin.psi.LombokLightFieldBuilder;
import de.plushnikov.intellij.plugin.util.PsiAnnotationUtil;
import de.plushnikov.intellij.plugin.util.PsiClassUtil;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Base lombok processor class for logger processing
 *
 * @author Plushnikov Michail
 */
public abstract class AbstractLogProcessor extends AbstractClassProcessor {
  enum LoggerInitializerParameter {
    TYPE,
    NAME,
    TOPIC,
    NULL,
    UNKNOWN;

    @Nonnull
    static LoggerInitializerParameter find(@Nonnull String parameter) {
      return switch (parameter) {
        case "TYPE" -> TYPE;
        case "NAME" -> NAME;
        case "TOPIC" -> TOPIC;
        case "NULL" -> NULL;
        default -> UNKNOWN;
      };
    }
  }

  AbstractLogProcessor(@Nonnull String supportedAnnotationClass) {
    super(PsiField.class, supportedAnnotationClass);
  }

  @Override
  protected Collection<String> getNamesOfPossibleGeneratedElements(@Nonnull PsiClass psiClass, @Nonnull PsiAnnotation psiAnnotation) {
    return Collections.singleton(getLoggerName(psiClass));
  }

  @Nonnull
  public static String getLoggerName(@Nonnull PsiClass psiClass) {
    return ConfigDiscovery.getInstance().getStringLombokConfigProperty(ConfigKey.LOG_FIELDNAME, psiClass);
  }

  public static boolean isLoggerStatic(@Nonnull PsiClass psiClass) {
    return ConfigDiscovery.getInstance().getBooleanLombokConfigProperty(ConfigKey.LOG_FIELD_IS_STATIC, psiClass);
  }

  /**
   * Nullable because it can be called before validation.
   */
  @Nullable
  public abstract String getLoggerType(@Nonnull PsiClass psiClass);

  /**
   * Call only after validation.
   */
  @Nonnull
  abstract String getLoggerInitializer(@Nonnull PsiClass psiClass);

  /**
   * Call only after validation.
   */
  @Nonnull
  abstract List<LoggerInitializerParameter> getLoggerInitializerParameters(@Nonnull PsiClass psiClass, boolean topicPresent);

  @Override
  protected boolean validate(@Nonnull PsiAnnotation psiAnnotation, @Nonnull PsiClass psiClass, @Nonnull ProblemSink builder) {
    boolean result = true;
    if (psiClass.isInterface() || psiClass.isAnnotationType()) {
      builder.addErrorMessage("inspection.message.s.legal.only.on.classes.enums", getSupportedAnnotationClasses()[0]);
      result = false;
    }
    if (result) {
      final String loggerName = getLoggerName(psiClass);
      if (hasFieldByName(psiClass, loggerName)) {
        builder.addErrorMessage("inspection.message.not.generating.field.s.field.with.same.name.already.exists", loggerName);
        result = false;
      }
    }
    return result;
  }

  @Override
  protected void generatePsiElements(@Nonnull PsiClass psiClass,
                                     @Nonnull PsiAnnotation psiAnnotation,
                                     @Nonnull List<? super PsiElement> target) {
    target.add(createLoggerField(psiClass, psiAnnotation));
  }

  private LombokLightFieldBuilder createLoggerField(@Nonnull PsiClass psiClass, @Nonnull PsiAnnotation psiAnnotation) {
    // called only after validation succeeded
    final Project project = psiClass.getProject();
    final PsiManager manager = psiClass.getContainingFile().getManager();

    final PsiElementFactory psiElementFactory = JavaPsiFacade.getElementFactory(project);
    String loggerType = getLoggerType(psiClass);
    if (loggerType == null) {
      throw new IllegalStateException("Invalid custom log declaration."); // validated
    }
    final PsiType psiLoggerType = psiElementFactory.createTypeFromText(loggerType, psiClass);

    LombokLightFieldBuilder loggerField = new LombokLightFieldBuilder(manager, getLoggerName(psiClass), psiLoggerType)
      .withContainingClass(psiClass)
      .withModifier(PsiModifier.FINAL)
      .withModifier(PsiModifier.PRIVATE)
      .withNavigationElement(psiAnnotation);
    if (isLoggerStatic(psiClass)) {
      loggerField.withModifier(PsiModifier.STATIC);
    }

    final String loggerInitializerParameters = createLoggerInitializeParameters(psiClass, psiAnnotation);
    final String initializerText = String.format(getLoggerInitializer(psiClass), loggerInitializerParameters);
    final PsiExpression initializer = psiElementFactory.createExpressionFromText(initializerText, psiClass);
    loggerField.setInitializer(initializer);
    return loggerField;
  }

  @Nonnull
  private String createLoggerInitializeParameters(@Nonnull PsiClass psiClass, @Nonnull PsiAnnotation psiAnnotation) {
    final StringBuilder parametersBuilder = new StringBuilder();
    final String topic = PsiAnnotationUtil.getStringAnnotationValue(psiAnnotation, "topic", "");
    final boolean topicPresent = !StringUtil.isEmptyOrSpaces(topic);
    final List<LoggerInitializerParameter> loggerInitializerParameters = getLoggerInitializerParameters(psiClass, topicPresent);
    for (LoggerInitializerParameter loggerInitializerParameter : loggerInitializerParameters) {
      if (parametersBuilder.length() > 0) {
        parametersBuilder.append(", ");
      }
      switch (loggerInitializerParameter) {
        case TYPE -> parametersBuilder.append(psiClass.getName()).append(".class");
        case NAME -> parametersBuilder.append(psiClass.getName()).append(".class.getName()");
        case TOPIC -> {
          if (!topicPresent) {
            // sanity check; either implementation of CustomLogParser or predefined loggers is wrong
            throw new IllegalStateException("Topic can never be a parameter when topic was not set.");
          }
          parametersBuilder.append('"').append(StringUtil.escapeStringCharacters(topic)).append('"');
        }
        case NULL -> parametersBuilder.append("null");
        default ->
          // sanity check; either implementation of CustomLogParser or predefined loggers is wrong
          throw new IllegalStateException("Unexpected logger initializer parameter " + loggerInitializerParameter);
      }
    }
    return parametersBuilder.toString();
  }

  private static boolean hasFieldByName(@Nonnull PsiClass psiClass, @Nonnull String fieldName) {
    final Collection<PsiField> psiFields = PsiClassUtil.collectClassFieldsIntern(psiClass);
    for (PsiField psiField : psiFields) {
      if (fieldName.equals(psiField.getName())) {
        return true;
      }
    }
    return false;
  }
}
