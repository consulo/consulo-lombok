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
package consulo.lombok.processors;

import java.util.List;

import javax.annotation.Nonnull;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import consulo.module.extension.ModuleExtension;

/**
 * @author VISTALL
 * @since 18:43/29.03.13
 */
public interface LombokProcessor {

  void process(@Nonnull PsiClass element, @Nonnull List<PsiElement> result);

  void collectInspections(@Nonnull PsiClass element, @Nonnull ProblemsHolder problemsHolder);

  @Nonnull
  Class<? extends PsiElement> getCollectorPsiElementClass();

  @Nonnull
  Class<? extends ModuleExtension> getModuleExtensionClass();
}
