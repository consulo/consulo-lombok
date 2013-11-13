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
package org.consulo.lombok.pg.processors.impl;

import com.intellij.codeInsight.generation.OverrideImplementExploreUtil;
import com.intellij.codeInsight.generation.OverrideImplementUtil;
import com.intellij.psi.*;
import com.intellij.psi.impl.light.LightMethodBuilder;
import com.intellij.psi.impl.light.LightTypeParameter;
import com.intellij.psi.infos.CandidateInfo;
import com.intellij.psi.util.MethodSignature;
import org.consulo.lombok.pg.processors.LombokPgSelfClassProcessor;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author VISTALL
 * @since 15:55/30.03.13
 */
public class AutoGenMethodStubProcessor extends LombokPgSelfClassProcessor {
  public AutoGenMethodStubProcessor(String annotationClass) {
    super(annotationClass);
  }

  @Override
  public void processElement(@NotNull PsiClass parent, @NotNull PsiClass psiClass, @NotNull List<PsiElement> result) {
    // we cant call psiClass.getMethods() - stackoverflow

    final Map<MethodSignature, PsiMethod> abstracts = new HashMap<MethodSignature, PsiMethod>();

    for (PsiClass superClass : psiClass.getSupers()) {
      if (superClass.hasModifierProperty(PsiModifier.ABSTRACT)) {
        for (PsiMethod method : superClass.getMethods()) {
          if (method.hasModifierProperty(PsiModifier.ABSTRACT)) {
            abstracts.put(method.getHierarchicalMethodSignature(), method);
          }
        }
      }
    }

    OverrideImplementExploreUtil
      .collectMethodsToImplement(null, abstracts, new HashMap<MethodSignature, PsiMethod>(), new HashMap<MethodSignature, PsiMethod>(),
                                 new TreeMap<MethodSignature, CandidateInfo>(new OverrideImplementUtil.MethodSignatureComparator()));

    for (Map.Entry<MethodSignature, PsiMethod> entry : abstracts.entrySet()) {
      final MethodSignature key = entry.getKey();
      final PsiMethod value = entry.getValue();

      LightMethodBuilder methodBuilder = new LightMethodBuilder(psiClass.getManager(), psiClass.getLanguage(), key.getName());
      methodBuilder.setContainingClass(psiClass);
      methodBuilder.setNavigationElement(getAffectedAnnotation(psiClass));

      for(PsiTypeParameter typeParameter : key.getTypeParameters()) {
        methodBuilder.addTypeParameter(new LightTypeParameter(typeParameter));
      }

      int i = 0;
      for(PsiParameter parameter : value.getParameterList().getParameters()) {
        String parameterName = parameter.getName();
        methodBuilder.addParameter(parameterName == null ? "p" + i : parameterName, key.getSubstitutor().substitute(parameter.getType()));

        i ++;
      }
      methodBuilder.setMethodReturnType(key.getSubstitutor().substitute(value.getReturnType()));

      result.add(methodBuilder);
    }
  }

  @NotNull
  @Override
  public Class<? extends PsiElement> getCollectorPsiElementClass() {
    return PsiMethod.class;
  }
}
