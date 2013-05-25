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
package org.consulo.lombok.processors.impl;

import com.intellij.psi.*;
import com.intellij.psi.util.MethodSignature;
import com.intellij.psi.util.MethodSignatureUtil;
import org.jetbrains.annotations.NotNull;

/**
 * @author VISTALL
 * @since 20:53/30.03.13
 */
public class ToStringAnnotationProcessor extends MethodCreatorByAnnotationProcessor {
  public ToStringAnnotationProcessor(String annotationClass) {
    super(annotationClass);
  }

  @NotNull
  @Override
  public MethodSignature[] getMethodSignatures(PsiClass psiClass) {
    return new MethodSignature[]{
      MethodSignatureUtil.createMethodSignature("toString", PsiType.EMPTY_ARRAY, PsiTypeParameter.EMPTY_ARRAY, PsiSubstitutor.EMPTY, false)
    };
  }

  @NotNull
  @Override
  public PsiType[] getReturnTypes(PsiClass psiClass) {
    return new PsiType[] {PsiType.getJavaLangString(psiClass.getManager(), psiClass.getResolveScope())};
  }
}
