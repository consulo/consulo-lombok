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

import org.consulo.lombok.processors.LombokFieldProcessor;
import org.consulo.lombok.processors.util.LombokClassUtil;
import org.consulo.lombok.processors.util.LombokUtil;
import com.intellij.psi.*;
import com.intellij.psi.impl.light.LightMethodBuilder;
import com.intellij.psi.util.MethodSignatureUtil;
import com.intellij.util.containers.MultiMap;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.List;

/**
 * @author VISTALL
 * @since 21:40/30.03.13
 */
public class DelegateAnnotationProcessor extends LombokFieldProcessor {
  public DelegateAnnotationProcessor(String annotationClass) {
    super(annotationClass);
  }

  @Override
  public void processElement(@NotNull PsiClass parent, @NotNull PsiField psiField, @NotNull List<PsiElement> result) {
    PsiAnnotation affected = getAffectedAnnotation(psiField);

    MultiMap<PsiClass, PsiMethod> toDelegateMethods = null;
    final PsiAnnotationMemberValue attributeValue = affected.findAttributeValue("types");
    if(attributeValue instanceof PsiArrayInitializerMemberValue) {
      PsiAnnotationMemberValue[] initializers = ((PsiArrayInitializerMemberValue)attributeValue).getInitializers();
      if(initializers.length == 0) {
        toDelegateMethods = LombokClassUtil.collectMethodsOfClass(parent, psiField.getType());
      }
      else {
        toDelegateMethods = LombokClassUtil.collectMethods(parent, attributeValue);
      }
    }
    else {
      toDelegateMethods = LombokClassUtil.collectMethodsOfClass(parent, psiField.getType());
    }

    final MultiMap<PsiClass, PsiMethod> toUnDelegateMethods =
      LombokClassUtil.collectMethods(parent, affected.findAttributeValue("excludes"));

    if (toDelegateMethods.isEmpty()) {
      LombokClassUtil.collectMethodsOfClass(parent, toDelegateMethods, psiField.getType());
    }

    for (PsiMethod ex : toUnDelegateMethods.values()) {
      Iterator<? extends PsiMethod> iterator = toDelegateMethods.values().iterator();
      while (iterator.hasNext()) {
        PsiMethod to = iterator.next();
        if (MethodSignatureUtil.areSignaturesEqual(ex, to)) {
          iterator.remove();
        }
      }
    }

    for (PsiMethod method : toDelegateMethods.values()) {
      LightMethodBuilder methodBuilder = new LightMethodBuilder(parent.getManager(), parent.getLanguage(), method.getName());
      LombokUtil.copyAccessModifierFromOriginal(method, methodBuilder);

      int i = 0;
      for (PsiParameter parameter : method.getParameterList().getParameters()) {
        String parameterName = parameter.getName();
        methodBuilder.addParameter(parameterName == null ? "p" + i : parameterName, PsiSubstitutor.EMPTY.substitute(parameter.getType()));
        i++;
      }

      methodBuilder.setMethodReturnType(PsiSubstitutor.EMPTY.substitute(method.getReturnType()));

      methodBuilder.setContainingClass(parent);
      methodBuilder.setNavigationElement(getAffectedAnnotation(psiField));

      result.add(methodBuilder);
    }
  }

  @NotNull
  @Override
  public Class<? extends PsiElement> getCollectorPsiElementClass() {
    return PsiMethod.class;
  }
}
