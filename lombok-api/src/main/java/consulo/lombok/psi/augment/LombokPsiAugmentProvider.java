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
package consulo.lombok.psi.augment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.augment.PsiAugmentProvider;
import consulo.lombok.processors.LombokProcessor;
import consulo.lombok.processors.LombokProcessorEP;
import consulo.lombok.processors.util.LombokUtil;
import consulo.module.extension.ModuleExtension;

/**
 * @author VISTALL
 * @since 18:56/29.03.13
 */
public abstract class LombokPsiAugmentProvider extends PsiAugmentProvider {
  @Nonnull
  @Override
  public <Psi extends PsiElement> List<Psi> getAugments(@Nonnull PsiElement element, @Nonnull Class<Psi> type) {
    if(!LombokUtil.isExtensionEnabled(element, getModuleExtensionClass())) {
      return Collections.emptyList();
    }

    List<Psi> list = new ArrayList<Psi>();

    for(LombokProcessorEP ep : LombokProcessorEP.EP_NAME.getExtensionList()) {
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

  @Nonnull
  protected abstract Class<? extends ModuleExtension> getModuleExtensionClass();
}
