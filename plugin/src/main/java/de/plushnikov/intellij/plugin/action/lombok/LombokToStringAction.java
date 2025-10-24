package de.plushnikov.intellij.plugin.action.lombok;

import consulo.annotation.component.ActionImpl;
import consulo.lombok.localize.LombokLocalize;

@ActionImpl(id = "defaultLombokToString")
public class LombokToStringAction extends BaseLombokAction {
    public LombokToStringAction() {
        super(new LombokToStringHandler(), LombokLocalize.actionToStringText(), LombokLocalize.actionToStringDescription());
    }
}
