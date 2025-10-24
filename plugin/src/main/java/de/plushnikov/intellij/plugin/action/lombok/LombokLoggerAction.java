package de.plushnikov.intellij.plugin.action.lombok;

import consulo.annotation.component.ActionImpl;
import consulo.lombok.localize.LombokLocalize;

@ActionImpl(id = "defaultLombokLogger")
public class LombokLoggerAction extends BaseLombokAction {
    public LombokLoggerAction() {
        super(new LombokLoggerHandler(), LombokLocalize.actionLoggerText(), LombokLocalize.actionLoggerDescription());
    }
}
