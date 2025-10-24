package de.plushnikov.intellij.plugin.action.delombok;

import consulo.annotation.component.ActionImpl;
import consulo.lombok.localize.LombokLocalize;
import consulo.lombok.processor.ProcessorUtil;
import de.plushnikov.intellij.plugin.processor.clazz.EqualsAndHashCodeProcessor;

@ActionImpl(id = "delombokEqualsAndHashCode")
public class DelombokEqualsAndHashCodeAction extends AbstractDelombokAction {
    public DelombokEqualsAndHashCodeAction() {
        super(LombokLocalize.actionDelombokEqualsAndHashCodeText(), LombokLocalize.actionDelombokEqualsAndHashCodeText());
    }

    @Override
    protected DelombokHandler createHandler() {
        return new DelombokHandler(ProcessorUtil.getProcessor(EqualsAndHashCodeProcessor.class));
    }
}
