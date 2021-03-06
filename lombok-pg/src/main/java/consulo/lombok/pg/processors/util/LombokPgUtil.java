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

package consulo.lombok.pg.processors.util;

import javax.annotation.Nonnull;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.psi.PsiElement;
import consulo.lombok.pg.module.extension.LombokPgModuleExtension;

/**
 * @author VISTALL
 * @since 18:48/25.05.13
 */
public class LombokPgUtil {
  public static boolean isLombokPgExtensionEnabled(@Nonnull PsiElement element) {
    Module moduleForPsiElement = ModuleUtilCore.findModuleForPsiElement(element);
    if(moduleForPsiElement == null) {
      return false;
    }
    return ModuleUtil.getExtension(moduleForPsiElement, LombokPgModuleExtension.class) != null;
  }
}
