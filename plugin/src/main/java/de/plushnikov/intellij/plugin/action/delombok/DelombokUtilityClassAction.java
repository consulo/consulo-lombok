package de.plushnikov.intellij.plugin.action.delombok;

import consulo.annotation.component.ActionImpl;
import consulo.lombok.localize.LombokLocalize;
import consulo.lombok.processor.ProcessorUtil;
import de.plushnikov.intellij.plugin.processor.clazz.UtilityClassProcessor;
import jakarta.annotation.Nonnull;

@ActionImpl(id = "delombokUtilityClass")
public class DelombokUtilityClassAction extends AbstractDelombokAction {
    public DelombokUtilityClassAction() {
        super(LombokLocalize.actionDelombokUtilityClassText(), LombokLocalize.actionDelombokUtilityClassDescription());
    }

    @Nonnull
    @Override
    protected DelombokHandler createHandler() {
        return new DelombokHandler(true, ProcessorUtil.getProcessor(UtilityClassProcessor.class));
    }
}
