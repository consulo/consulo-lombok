package de.plushnikov.intellij.plugin.action.delombok;

import consulo.annotation.component.ActionImpl;
import consulo.lombok.localize.LombokLocalize;
import consulo.lombok.processor.ProcessorUtil;
import de.plushnikov.intellij.plugin.processor.clazz.ValueProcessor;
import jakarta.annotation.Nonnull;

@ActionImpl(id = "delombokValue")
public class DelombokValueAction extends AbstractDelombokAction {
    public DelombokValueAction() {
        super(LombokLocalize.actionDelombokValueText(), LombokLocalize.actionDelombokValueDescription());
    }

    @Nonnull
    @Override
    protected DelombokHandler createHandler() {
        return new DelombokHandler(ProcessorUtil.getProcessor(ValueProcessor.class));
    }
}
