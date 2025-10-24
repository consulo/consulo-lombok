package de.plushnikov.intellij.plugin.action.delombok;

import consulo.annotation.component.ActionImpl;
import consulo.lombok.localize.LombokLocalize;
import consulo.lombok.processor.ProcessorUtil;
import de.plushnikov.intellij.plugin.processor.clazz.StandardExceptionProcessor;
import jakarta.annotation.Nonnull;

@ActionImpl(id = "delombokStandardException")
public class DelombokStandardExceptionAction extends AbstractDelombokAction {
    public DelombokStandardExceptionAction() {
        super(LombokLocalize.actionDelombokStandardExceptionText(), LombokLocalize.actionDelombokStandardExceptionDescription());
    }

    @Nonnull
    @Override
    protected DelombokHandler createHandler() {
        return new DelombokHandler(true, ProcessorUtil.getProcessor(StandardExceptionProcessor.class));
    }
}
