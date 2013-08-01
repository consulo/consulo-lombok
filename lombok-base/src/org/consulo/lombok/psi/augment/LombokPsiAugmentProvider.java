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
package org.consulo.lombok.psi.augment;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.augment.PsiAugmentProvider;
import org.consulo.lombok.module.extension.LombokModuleExtension;
import org.consulo.lombok.processors.LombokProcessor;
import org.consulo.lombok.processors.LombokProcessorEP;
import org.consulo.lombok.processors.util.LombokUtil;
import org.consulo.module.extension.ModuleExtension;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author VISTALL
 * @since 18:56/29.03.13
 */
public class LombokPsiAugmentProvider extends PsiAugmentProvider {
  @NotNull
  @Override
  public <Psi extends PsiElement> List<Psi> getAugments(@NotNull PsiElement element, @NotNull Class<Psi> type) {
    if(!LombokUtil.isExtensionEnabled(element, getModuleExtensionClass())) {
      return Collections.emptyList();
    }

    List<Psi> list = new ArrayList<Psi>();

    for(LombokProcessorEP ep : LombokProcessorEP.EP_NAME.getExtensions()) {
      final LombokProcessor instance = ep.getInstance();
      if(instance.getModuleExtensionClass() != getModuleExtensionClass()) {
        continue;
      }

      if(instance.getCollectorPsiElementClass() == type) {
        instance.process((PsiClass)element, (List<PsiElement>)list);
      }
    }
    return list;
  }

  @NotNull
  protected Class<? extends ModuleExtension> getModuleExtensionClass() {
    return LombokModuleExtension.class;
  }
}
