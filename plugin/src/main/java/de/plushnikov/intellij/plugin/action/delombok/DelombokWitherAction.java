package de.plushnikov.intellij.plugin.action.delombok;

import consulo.annotation.component.ActionImpl;
import consulo.lombok.localize.LombokLocalize;
import consulo.lombok.processor.ProcessorUtil;
import de.plushnikov.intellij.plugin.processor.clazz.WitherProcessor;
import de.plushnikov.intellij.plugin.processor.field.WitherFieldProcessor;
import jakarta.annotation.Nonnull;

@ActionImpl(id = "delombokWither")
public class DelombokWitherAction extends AbstractDelombokAction {
    public DelombokWitherAction() {
        super(LombokLocalize.actionDelombokWitherText(), LombokLocalize.actionDelombokWitherDescription());
    }

    @Override
    @Nonnull
    protected DelombokHandler createHandler() {
        return new DelombokHandler(
            ProcessorUtil.getProcessor(WitherProcessor.class),
            ProcessorUtil.getProcessor(WitherFieldProcessor.class)
        );
    }
}
