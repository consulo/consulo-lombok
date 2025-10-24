package de.plushnikov.intellij.plugin.action.lombok;

import consulo.annotation.component.ActionImpl;
import consulo.lombok.localize.LombokLocalize;

@ActionImpl(id = "defaultLombokGetter")
public class LombokGetterAction extends BaseLombokAction {
    public LombokGetterAction() {
        super(new LombokGetterHandler(), LombokLocalize.actionGetterText(), LombokLocalize.actionGetterDescription());
    }
}
