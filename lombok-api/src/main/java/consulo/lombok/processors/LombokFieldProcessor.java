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

import com.intellij.java.language.psi.PsiClass;
import com.intellij.java.language.psi.PsiField;

import jakarta.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 18:54/29.03.13
 */
public abstract class LombokFieldProcessor extends LombokAnnotationOwnerProcessor<PsiField> {
  public LombokFieldProcessor(String annotationClass) {
    super(annotationClass);
  }

  @Nonnull
  @Override
  protected PsiField[] getElements(@Nonnull PsiClass psiClass) {
    return psiClass.getFields();
  }
}
