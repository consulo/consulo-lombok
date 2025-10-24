package de.plushnikov.intellij.plugin.action.delombok;

import consulo.annotation.component.ActionImpl;
import consulo.lombok.localize.LombokLocalize;
import consulo.lombok.processor.ProcessorUtil;
import de.plushnikov.intellij.plugin.processor.clazz.DataProcessor;
import jakarta.annotation.Nonnull;

@ActionImpl(id = "delombokData")
public class DelombokDataAction extends AbstractDelombokAction {
    public DelombokDataAction() {
        super(LombokLocalize.actionDelombokDataText(), LombokLocalize.actionDelombokDataDescription());
    }

    @Nonnull
    @Override
    protected DelombokHandler createHandler() {
        return new DelombokHandler(ProcessorUtil.getProcessor(DataProcessor.class));
    }
}
