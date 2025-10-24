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
import de.plushnikov.intellij.plugin.action.delombok.*;

/**
 * @author UNV
 * @since 2025-10-24
 */
@ActionImpl(
    id = "DelombokActionGroup",
    children = {
        @ActionRef(type = DelombokEverythingAction.class),
        @ActionRef(type = AnSeparator.class),
        @ActionRef(type = DelombokDataAction.class),
        @ActionRef(type = DelombokValueAction.class),
        @ActionRef(type = DelombokWitherAction.class),
        @ActionRef(type = DelombokDelegateAction.class),
        @ActionRef(type = DelombokBuilderAction.class),
        @ActionRef(type = DelombokSuperBuilderAction.class),
        @ActionRef(type = AnSeparator.class),
        @ActionRef(type = DelombokConstructorAction.class),
        @ActionRef(type = DelombokGetterAction.class),
        @ActionRef(type = DelombokSetterAction.class),
        @ActionRef(type = DelombokEqualsAndHashCodeAction.class),
        @ActionRef(type = DelombokToStringAction.class),
        @ActionRef(type = AnSeparator.class),
        @ActionRef(type = DelombokLoggerAction.class),
        @ActionRef(type = DelombokFieldNameConstantsAction.class),
        @ActionRef(type = DelombokUtilityClassAction.class),
        @ActionRef(type = DelombokStandardExceptionAction.class)
    }
)
public class DelombokActionGroup extends LombokMenuGroup {
    public DelombokActionGroup() {
        super(LombokLocalize.groupDelombokText(), LombokLocalize.groupDelombokDescription(), LombokIconGroup.lombok());
        setPopup(true);
    }
}
