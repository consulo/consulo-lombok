package de.plushnikov.intellij.plugin.action.delombok;

import consulo.annotation.component.ActionImpl;
import consulo.lombok.localize.LombokLocalize;
import consulo.lombok.processor.ProcessorUtil;
import de.plushnikov.intellij.plugin.processor.clazz.fieldnameconstants.FieldNameConstantsOldProcessor;
import de.plushnikov.intellij.plugin.processor.clazz.fieldnameconstants.FieldNameConstantsPredefinedInnerClassFieldProcessor;
import de.plushnikov.intellij.plugin.processor.clazz.fieldnameconstants.FieldNameConstantsProcessor;
import de.plushnikov.intellij.plugin.processor.field.FieldNameConstantsFieldProcessor;
import jakarta.annotation.Nonnull;

@ActionImpl(id = "delombokFieldNameConstants")
public class DelombokFieldNameConstantsAction extends AbstractDelombokAction {
    public DelombokFieldNameConstantsAction() {
        super(LombokLocalize.actionDelombokFieldNameConstantsText(), LombokLocalize.actionDelombokFieldNameConstantsDescription());
    }

    @Nonnull
    @Override
    protected DelombokHandler createHandler() {
        return new DelombokHandler(
            true,
            ProcessorUtil.getProcessor(FieldNameConstantsOldProcessor.class),
            ProcessorUtil.getProcessor(FieldNameConstantsFieldProcessor.class),
            ProcessorUtil.getProcessor(FieldNameConstantsProcessor.class),
            ProcessorUtil.getProcessor(FieldNameConstantsPredefinedInnerClassFieldProcessor.class)
        );
    }
}
