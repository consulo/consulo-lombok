package de.plushnikov.intellij.plugin.action.delombok;

import consulo.annotation.component.ActionImpl;
import consulo.lombok.localize.LombokLocalize;
import consulo.lombok.processor.ProcessorUtil;
import de.plushnikov.intellij.plugin.processor.field.DelegateFieldProcessor;
import de.plushnikov.intellij.plugin.processor.method.DelegateMethodProcessor;
import jakarta.annotation.Nonnull;

@ActionImpl(id = "delombokDelegate")
public class DelombokDelegateAction extends AbstractDelombokAction {
    public DelombokDelegateAction() {
        super(LombokLocalize.actionDelombokDelegateText(), LombokLocalize.actionDelombokDelegateText());
    }

    @Nonnull
    @Override
    protected DelombokHandler createHandler() {
        return new DelombokHandler(
            ProcessorUtil.getProcessor(DelegateFieldProcessor.class),
            ProcessorUtil.getProcessor(DelegateMethodProcessor.class)
        );
    }
}
