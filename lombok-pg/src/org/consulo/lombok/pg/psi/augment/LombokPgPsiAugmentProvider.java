/*
 * Copyright 2013 must-be.org
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

package org.consulo.lombok.pg.psi.augment;

import org.consulo.lombok.pg.module.extension.LombokPgModuleExtension;
import org.consulo.lombok.psi.augment.LombokPsiAugmentProvider;
import org.consulo.module.extension.ModuleExtension;
import org.jetbrains.annotations.NotNull;

/**
 * @author VISTALL
 * @since 01.08.13.
 */
public class LombokPgPsiAugmentProvider extends LombokPsiAugmentProvider {
  @NotNull
  @Override
  protected Class<? extends ModuleExtension> getModuleExtensionClass() {
    return LombokPgModuleExtension.class;
  }
}
