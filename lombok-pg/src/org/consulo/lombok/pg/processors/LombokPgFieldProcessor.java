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

package org.consulo.lombok.pg.processors;

import org.consulo.lombok.pg.module.extension.LombokPgModuleExtension;
import org.consulo.lombok.processors.LombokFieldProcessor;
import org.jetbrains.annotations.NotNull;
import consulo.module.extension.ModuleExtension;

/**
 * @author VISTALL
 * @since 18:56/25.05.13
 */
public abstract class LombokPgFieldProcessor extends LombokFieldProcessor {
  public LombokPgFieldProcessor(String annotationClass) {
    super(annotationClass);
  }

  @NotNull
  @Override
  public Class<? extends ModuleExtension> getModuleExtensionClass() {
    return LombokPgModuleExtension.class;
  }
}
