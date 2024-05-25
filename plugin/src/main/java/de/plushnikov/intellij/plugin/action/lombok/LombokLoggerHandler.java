package de.plushnikov.intellij.plugin.action.lombok;

import com.intellij.java.language.psi.PsiClass;
import com.intellij.java.language.psi.PsiField;
import com.intellij.java.language.psi.PsiModifier;
import consulo.language.editor.refactoring.rename.RenameProcessor;
import consulo.lombok.processor.ProcessorUtil;
import consulo.ui.ex.awt.DialogWrapper;
import consulo.ui.ex.awt.Messages;
import de.plushnikov.intellij.plugin.LombokBundle;
import de.plushnikov.intellij.plugin.processor.clazz.log.*;
import jakarta.annotation.Nonnull;

import java.util.Arrays;
import java.util.Collection;

public class LombokLoggerHandler extends BaseLombokHandler {

  @Override
  protected void processClass(@Nonnull PsiClass psiClass) {
    final Collection<AbstractLogProcessor> logProcessors = Arrays.asList(
      ProcessorUtil.getProcessor(CommonsLogProcessor.class),
      ProcessorUtil.getProcessor(JBossLogProcessor.class),
      ProcessorUtil.getProcessor(Log4jProcessor.class),
      ProcessorUtil.getProcessor(Log4j2Processor.class),
      ProcessorUtil.getProcessor(LogProcessor.class),
      ProcessorUtil.getProcessor(Slf4jProcessor.class),
      ProcessorUtil.getProcessor(XSlf4jProcessor.class),
      ProcessorUtil.getProcessor(FloggerProcessor.class),
      ProcessorUtil.getProcessor(CustomLogProcessor.class));

    final String lombokLoggerName = AbstractLogProcessor.getLoggerName(psiClass);
    final boolean lombokLoggerIsStatic = AbstractLogProcessor.isLoggerStatic(psiClass);

    for (AbstractLogProcessor logProcessor : logProcessors) {
      String loggerType = logProcessor.getLoggerType(psiClass); // null when the custom log's declaration is invalid
      if (loggerType == null) {
        continue;
      }
      for (PsiField psiField : psiClass.getFields()) {
        if (psiField.getType().equalsToText(loggerType) && checkLoggerField(psiField, lombokLoggerName, lombokLoggerIsStatic)) {
          processLoggerField(psiField, psiClass, logProcessor, lombokLoggerName);
        }
      }
    }
  }

  private static void processLoggerField(@Nonnull PsiField psiField,
                                         @Nonnull PsiClass psiClass,
                                         @Nonnull AbstractLogProcessor logProcessor,
                                         @Nonnull String lombokLoggerName) {
    if (!lombokLoggerName.equals(psiField.getName())) {
      RenameProcessor processor = new RenameProcessor(psiField.getProject(), psiField, lombokLoggerName, false, false);
      processor.doRun();
    }

    addAnnotation(psiClass, logProcessor.getSupportedAnnotationClasses()[0]);

    psiField.delete();
  }

  private static boolean checkLoggerField(@Nonnull PsiField psiField, @Nonnull String lombokLoggerName, boolean lombokLoggerIsStatic) {
    if (!isValidLoggerField(psiField, lombokLoggerName, lombokLoggerIsStatic)) {
      String messageText =
        LombokBundle.message("dialog.message.logger.field.s.not.private.sfinal.field.named.s.refactor.anyway", psiField.getName(),
                             lombokLoggerIsStatic ? 1 : 0, lombokLoggerName);
      int result = Messages.showOkCancelDialog(messageText, LombokBundle.message("dialog.title.attention"), Messages.getQuestionIcon());
      return DialogWrapper.OK_EXIT_CODE == result;
    }
    return true;
  }

  private static boolean isValidLoggerField(@Nonnull PsiField psiField, @Nonnull String lombokLoggerName, boolean lombokLoggerIsStatic) {
    boolean isPrivate = psiField.hasModifierProperty(PsiModifier.PRIVATE);
    boolean isStatic = lombokLoggerIsStatic == psiField.hasModifierProperty(PsiModifier.STATIC);
    boolean isFinal = psiField.hasModifierProperty(PsiModifier.FINAL);
    boolean isProperlyNamed = lombokLoggerName.equals(psiField.getName());

    return isPrivate && isStatic && isFinal && isProperlyNamed;
  }
}
