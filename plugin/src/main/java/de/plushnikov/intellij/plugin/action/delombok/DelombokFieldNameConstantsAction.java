package de.plushnikov.intellij.plugin.action.delombok;

import consulo.lombok.processor.ProcessorUtil;
import de.plushnikov.intellij.plugin.processor.clazz.fieldnameconstants.FieldNameConstantsOldProcessor;
import de.plushnikov.intellij.plugin.processor.clazz.fieldnameconstants.FieldNameConstantsPredefinedInnerClassFieldProcessor;
import de.plushnikov.intellij.plugin.processor.clazz.fieldnameconstants.FieldNameConstantsProcessor;
import de.plushnikov.intellij.plugin.processor.field.FieldNameConstantsFieldProcessor;
import jakarta.annotation.Nonnull;

public class DelombokFieldNameConstantsAction extends AbstractDelombokAction {
  @Override
  @Nonnull
  protected DelombokHandler createHandler() {
    return new DelombokHandler(true,
                               ProcessorUtil.getProcessor(FieldNameConstantsOldProcessor.class),
                               ProcessorUtil.getProcessor(FieldNameConstantsFieldProcessor.class),
                               ProcessorUtil.getProcessor(FieldNameConstantsProcessor.class),
                               ProcessorUtil.getProcessor(FieldNameConstantsPredefinedInnerClassFieldProcessor.class));
  }
}
