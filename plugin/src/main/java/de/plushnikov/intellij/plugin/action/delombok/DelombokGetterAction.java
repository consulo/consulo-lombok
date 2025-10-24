package de.plushnikov.intellij.plugin.action.delombok;

import consulo.annotation.component.ActionImpl;
import consulo.lombok.localize.LombokLocalize;
import consulo.lombok.processor.ProcessorUtil;
import de.plushnikov.intellij.plugin.processor.clazz.GetterProcessor;
import de.plushnikov.intellij.plugin.processor.field.GetterFieldProcessor;
import jakarta.annotation.Nonnull;

@ActionImpl(id = "delombokGetter")
public class DelombokGetterAction extends AbstractDelombokAction {
    public DelombokGetterAction() {
        super(LombokLocalize.actionDelombokGetterText(), LombokLocalize.actionDelombokGetterDescription());
    }

    @Override
    @Nonnull
    protected DelombokHandler createHandler() {
        return new DelombokHandler(
            ProcessorUtil.getProcessor(GetterProcessor.class),
            ProcessorUtil.getProcessor(GetterFieldProcessor.class)
        );
    }
}
