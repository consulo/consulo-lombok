package de.plushnikov.intellij.plugin.action.delombok;

import consulo.annotation.component.ActionImpl;
import consulo.lombok.localize.LombokLocalize;
import consulo.lombok.processor.ProcessorUtil;
import de.plushnikov.intellij.plugin.processor.clazz.ToStringProcessor;
import jakarta.annotation.Nonnull;

@ActionImpl(id = "delombokToString")
public class DelombokToStringAction extends AbstractDelombokAction {
    public DelombokToStringAction() {
        super(LombokLocalize.actionDelombokToStringText(), LombokLocalize.actionDelombokToStringDescription());
    }

    @Override
    @Nonnull
    protected DelombokHandler createHandler() {
        return new DelombokHandler(ProcessorUtil.getProcessor(ToStringProcessor.class));
    }
}
