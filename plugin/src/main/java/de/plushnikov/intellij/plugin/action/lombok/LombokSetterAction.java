package de.plushnikov.intellij.plugin.action.lombok;

import consulo.annotation.component.ActionImpl;
import consulo.lombok.localize.LombokLocalize;

@ActionImpl(id = "defaultLombokSetter")
public class LombokSetterAction extends BaseLombokAction {
    public LombokSetterAction() {
        super(new LombokSetterHandler(), LombokLocalize.actionSetterText(), LombokLocalize.actionSetterDescription());
    }
}
