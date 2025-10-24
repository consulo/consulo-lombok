package de.plushnikov.intellij.plugin.action.lombok;

import consulo.annotation.component.ActionImpl;
import consulo.lombok.localize.LombokLocalize;

@ActionImpl(id = "defaultLombokEqualsAndHashcode")
public class LombokEqualsAndHashcodeAction extends BaseLombokAction {

    public LombokEqualsAndHashcodeAction() {
        super(
            new LombokEqualsAndHashcodeHandler(),
            LombokLocalize.actionEqualsAndHashCodeText(),
            LombokLocalize.actionEqualsAndHashCodeDescription()
        );
    }

}
