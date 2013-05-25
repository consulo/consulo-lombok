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
package org.consulo.lombok.pg.processors.impl;

import com.intellij.psi.*;
import com.intellij.psi.impl.light.LightMethodBuilder;
import org.consulo.lombok.pg.processors.LombokPgFieldProcessor;
import org.consulo.lombok.processors.impl.SetterAnnotationProcessor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * @author VISTALL
 * @since 12:39/31.03.13
 */
public class BoundSetterAnnotationProcessor extends LombokPgFieldProcessor {
  private static final String[] ourListenerNames = {"addPropertyChangeListener", "removePropertyChangeListener"};
  private static final String JAVA_BEAN_PROPERTY_CHANGE_LISTENER = "java.beans.PropertyChangeListener";

  private SetterAnnotationProcessor mySetterAnnotationProcessor;

  public BoundSetterAnnotationProcessor(String annotationClass) {
    super(annotationClass);
    mySetterAnnotationProcessor = new SetterAnnotationProcessor(annotationClass);
  }

  @Override
  public void processElement(@NotNull PsiClass parent, @NotNull PsiField psiField, @NotNull List<PsiElement> result) {
    final PsiAnnotation affectedAnnotation = getAffectedAnnotation(psiField);

    for(String name : ourListenerNames) {
      LightMethodBuilder builder = new LightMethodBuilder(parent.getManager(), parent.getLanguage(), name);
      builder.addModifier(PsiModifier.PUBLIC);
      if(psiField.hasModifierProperty(PsiModifier.STATIC)) {
        builder.addModifier(PsiModifier.STATIC);
      }
      builder.setMethodReturnType(PsiType.VOID);
      builder.setNavigationElement(affectedAnnotation);
      builder.setContainingClass(parent);
      builder.addParameter("listener", JavaPsiFacade.getElementFactory(parent.getProject()).createTypeByFQClassName(
        JAVA_BEAN_PROPERTY_CHANGE_LISTENER));

      result.add(builder);
    }

    mySetterAnnotationProcessor.processElement(parent, psiField, result);
  }

  @NotNull
  @Override
  public Class<? extends PsiElement> getCollectorPsiElementClass() {
    return PsiMethod.class;
  }
}
