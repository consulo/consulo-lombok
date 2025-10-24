package de.plushnikov.intellij.plugin.action.delombok;

import consulo.annotation.component.ActionImpl;
import consulo.lombok.localize.LombokLocalize;
import consulo.lombok.processor.ProcessorUtil;
import de.plushnikov.intellij.plugin.processor.clazz.constructor.AllArgsConstructorProcessor;
import de.plushnikov.intellij.plugin.processor.clazz.constructor.NoArgsConstructorProcessor;
import de.plushnikov.intellij.plugin.processor.clazz.constructor.RequiredArgsConstructorProcessor;
import jakarta.annotation.Nonnull;

@ActionImpl(id = "delombokConstructor")
public class DelombokConstructorAction extends AbstractDelombokAction {
    public DelombokConstructorAction() {
        super(LombokLocalize.actionDelombokConstructorText(), LombokLocalize.actionDelombokConstructorText());
    }

    @Nonnull
    @Override
    protected DelombokHandler createHandler() {
        return new DelombokHandler(
            ProcessorUtil.getProcessor(AllArgsConstructorProcessor.class),
            ProcessorUtil.getProcessor(NoArgsConstructorProcessor.class),
            ProcessorUtil.getProcessor(RequiredArgsConstructorProcessor.class)
        );
    }
}
