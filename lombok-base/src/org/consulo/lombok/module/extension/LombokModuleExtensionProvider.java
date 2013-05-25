/*
 * Copyright 2013 Consulo.org
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

package org.consulo.lombok.module.extension;

import javax.swing.Icon;

import org.consulo.lombok.LombokIcons;
import org.consulo.module.extension.ModuleExtensionProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.openapi.module.Module;

/**
 * @author VISTALL
 * @since 25.05.13
 */
public class LombokModuleExtensionProvider implements ModuleExtensionProvider<LombokModuleExtension, LombokMutableModuleExtension> {
  @Nullable
  @Override
  public Icon getIcon() {
    return LombokIcons.ICON;
  }

  @NotNull
  @Override
  public String getName() {
    return "Lombok";
  }

  @Override
  public Class<LombokModuleExtension> getImmutableClass() {
    return LombokModuleExtension.class;
  }

  @Override
  public LombokModuleExtension createImmutable(@NotNull String s, @NotNull Module module) {
    return null;
  }

  @Override
  public LombokMutableModuleExtension createMutable(@NotNull String s,
                                                    @NotNull Module module,
                                                    @NotNull LombokModuleExtension lombokModuleExtension) {
    return new LombokMutableModuleExtension(s, module, lombokModuleExtension);
  }
}
