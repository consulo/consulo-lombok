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
package org.consulo.lombok.processors.impl;

import com.intellij.psi.PsiField;
import org.jetbrains.annotations.NotNull;

/**
 * @author VISTALL
 * @since 22:10/30.03.13
 */
public class NoArgsConstructorAnnotationProcessor extends NArgsConstructorAnnotationProcessor {
  public NoArgsConstructorAnnotationProcessor(String annotationClass) {
    super(annotationClass);
  }

  @Override
  protected boolean isFieldIsApplicable(@NotNull PsiField psiField) {
    return false;
  }
}
