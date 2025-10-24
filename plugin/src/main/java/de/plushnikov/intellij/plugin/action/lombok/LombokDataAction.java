package de.plushnikov.intellij.plugin.action.lombok;

import consulo.annotation.component.ActionImpl;
import consulo.lombok.localize.LombokLocalize;

@ActionImpl(id = "defaultLombokData")
public class LombokDataAction extends BaseLombokAction {
    public LombokDataAction() {
        super(new LombokDataHandler(), LombokLocalize.actionDataText(), LombokLocalize.actionDataDescription());
    }
}
