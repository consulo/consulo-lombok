package de.plushnikov.intellij.plugin.action.delombok;

import consulo.annotation.component.ActionImpl;
import consulo.lombok.localize.LombokLocalize;
import consulo.lombok.processor.ProcessorUtil;
import de.plushnikov.intellij.plugin.processor.clazz.SetterProcessor;
import de.plushnikov.intellij.plugin.processor.field.SetterFieldProcessor;
import jakarta.annotation.Nonnull;

@ActionImpl(id = "delombokSetter")
public class DelombokSetterAction extends AbstractDelombokAction {
    public DelombokSetterAction() {
        super(LombokLocalize.actionDelombokSetterText(), LombokLocalize.actionDelombokSetterDescription());
    }

    @Override
    @Nonnull
    protected DelombokHandler createHandler() {
        return new DelombokHandler(
            ProcessorUtil.getProcessor(SetterProcessor.class),
            ProcessorUtil.getProcessor(SetterFieldProcessor.class)
        );
    }
}
