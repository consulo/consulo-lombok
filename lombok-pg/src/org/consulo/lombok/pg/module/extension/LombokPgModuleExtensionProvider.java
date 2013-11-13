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

package org.consulo.lombok.pg.module.extension;

import com.intellij.openapi.module.Module;
import org.consulo.lombok.pg.LombokPgIcons;
import org.consulo.module.extension.ModuleExtensionProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * @author VISTALL
 * @since 25.05.13
 */
public class LombokPgModuleExtensionProvider implements ModuleExtensionProvider<LombokPgModuleExtension, LombokPgMutableModuleExtension> {
  @Nullable
  @Override
  public Icon getIcon() {
    return LombokPgIcons.ICON;
  }

  @NotNull
  @Override
  public String getName() {
    return "Lombok-pg";
  }

  @Override
  public Class<LombokPgModuleExtension> getImmutableClass() {
    return LombokPgModuleExtension.class;
  }

  @Override
  public LombokPgModuleExtension createImmutable(@NotNull String s, @NotNull Module module) {
    return new LombokPgModuleExtension(s, module);
  }

  @Override
  public LombokPgMutableModuleExtension createMutable(@NotNull String s,
                                                    @NotNull Module module,
                                                    @NotNull LombokPgModuleExtension lombokModuleExtension) {
    return new LombokPgMutableModuleExtension(s, module, lombokModuleExtension);
  }
}
