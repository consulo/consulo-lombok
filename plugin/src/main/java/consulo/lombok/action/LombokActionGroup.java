/*
 * Copyright 2013-2025 consulo.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package consulo.lombok.action;

import consulo.annotation.component.ActionImpl;
import consulo.annotation.component.ActionRef;
import consulo.lombok.impl.icon.LombokIconGroup;
import consulo.lombok.localize.LombokLocalize;
import consulo.ui.ex.action.AnSeparator;
import de.plushnikov.intellij.plugin.action.LombokMenuGroup;
import de.plushnikov.intellij.plugin.action.lombok.*;

/**
 * @author UNV
 * @since 2025-10-24
 */
@ActionImpl(
    id = "LombokActionGroup",
    children = {
        @ActionRef(type = LombokDataAction.class),
        @ActionRef(type = AnSeparator.class),
        @ActionRef(type = LombokGetterAction.class),
        @ActionRef(type = LombokSetterAction.class),
        @ActionRef(type = LombokEqualsAndHashcodeAction.class),
        @ActionRef(type = LombokToStringAction.class),
        @ActionRef(type = AnSeparator.class),
        @ActionRef(type = LombokLoggerAction.class)
    }
)
public class LombokActionGroup extends LombokMenuGroup {
    public LombokActionGroup() {
        super(LombokLocalize.groupLombokText(), LombokLocalize.groupLombokDescription(), LombokIconGroup.lombok());
        setPopup(true);
    }
}
