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

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.*;
import com.intellij.psi.impl.light.LightMethodBuilder;
import com.intellij.psi.impl.light.LightParameter;
import com.intellij.psi.impl.source.PsiImmediateClassType;
import com.intellij.util.containers.MultiMap;
import org.consulo.lombok.pg.processors.LombokPgSelfClassProcessor;
import org.consulo.lombok.processors.util.LombokClassUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * @author VISTALL
 * @since 22:36/30.03.13
 */
public class ListenerSupportAnnotationProcessor extends LombokPgSelfClassProcessor {
  private static final String[] ourCollectionMethodsPrefix = {"add", "remove"};

  public ListenerSupportAnnotationProcessor(String annotationClass) {
    super(annotationClass);
  }

  @Override
  public void processElement(@NotNull PsiClass parent, @NotNull PsiClass psiClass, @NotNull List<PsiElement> result) {
    final PsiAnnotation affectedAnnotation = getAffectedAnnotation(psiClass);

    final PsiAnnotationMemberValue attributeValue = affectedAnnotation.findAttributeValue(PsiAnnotation.DEFAULT_REFERENCED_METHOD_NAME);
    if(attributeValue == null) {
      return;
    }

    final MultiMap<PsiClass, PsiMethod> classToMethods = LombokClassUtil.collectMethods(parent, attributeValue);

    for (PsiClass c : classToMethods.keySet()) {
      for(String prefix : ourCollectionMethodsPrefix) {
        LightMethodBuilder builder = new LightMethodBuilder(psiClass.getManager(), parent.getLanguage(), prefix + c.getName());
        builder.addModifier(PsiModifier.PUBLIC);
        builder.setMethodReturnType(PsiType.VOID);
        builder.setNavigationElement(affectedAnnotation);
        builder.setContainingClass(psiClass);
        builder.addParameter("listener", new PsiImmediateClassType(c, PsiSubstitutor.EMPTY));

        result.add(builder);
      }
    }

    for(PsiMethod method : classToMethods.values()) {
      LightMethodBuilder builder = new LightMethodBuilder(psiClass.getManager(), parent.getLanguage(), "fire" + StringUtil.capitalize(method.getName()));
      builder.addModifier(PsiModifier.PUBLIC);
      builder.setMethodReturnType(method.getReturnType());
      builder.setNavigationElement(affectedAnnotation);
      builder.setContainingClass(psiClass);

      int i = 0;
      for(PsiParameter parameter : method.getParameterList().getParameters()) {
        String parameterName = parameter.getName();
        builder.addParameter(new LightParameter(parameterName == null ? "p" + i : parameterName, parameter.getType(), builder, builder.getLanguage()));
        i ++;
      }

      result.add(builder);
    }
  }

  @NotNull
  @Override
  public Class<? extends PsiElement> getCollectorPsiElementClass() {
    return PsiMethod.class;
  }
}
