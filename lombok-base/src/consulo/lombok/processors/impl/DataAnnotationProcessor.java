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

package consulo.lombok.processors.impl;

import java.util.List;

import org.jetbrains.annotations.NotNull;
import com.intellij.codeInsight.AnnotationUtil;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifier;
import com.intellij.psi.PsiModifierListOwner;
import consulo.lombok.LombokClassNames;
import consulo.lombok.processors.LombokSelfClassProcessor;
import consulo.lombok.processors.util.LombokClassUtil;

/**
 * @author VISTALL
 * @since 21:00/30.04.13
 */
public class DataAnnotationProcessor extends LombokSelfClassProcessor
{
  private AllArgsConstructorAnnotationProcessor myConstructorProcessor;
  private ToStringAnnotationProcessor myToStringAnnotationProcessor;
  private EqualsAndHashCodeAnnotationProcessor myEqualsAndHashCodeProcessor;
  private SetterAnnotationProcessor mySetterProcessor;
  private GetterAnnotationProcessor myGetterProcessor;

  public DataAnnotationProcessor(String annotationClass) {
    super(annotationClass);
    myConstructorProcessor = new AllArgsConstructorAnnotationProcessor(annotationClass) {
      @NotNull
      @Override
      protected String getStaticConstructorAttributeName() {
        return "staticConstructor";
      }
    };
    myToStringAnnotationProcessor = new ToStringAnnotationProcessor(annotationClass);
    myEqualsAndHashCodeProcessor = new EqualsAndHashCodeAnnotationProcessor(annotationClass);
    mySetterProcessor = new SetterAnnotationProcessor(annotationClass){
      @NotNull
      @Override
      public PsiAnnotation getAffectedAnnotation(PsiModifierListOwner owner) {
        return DataAnnotationProcessor.this.getAffectedAnnotation((PsiClass) owner.getParent());
      }
    };
    myGetterProcessor = new GetterAnnotationProcessor(annotationClass){
      @NotNull
      @Override
      public PsiAnnotation getAffectedAnnotation(PsiModifierListOwner owner) {
        return DataAnnotationProcessor.this.getAffectedAnnotation((PsiClass) owner.getParent());
      }
    };
  }

  @Override
  public void processElement(@NotNull PsiClass parent, @NotNull PsiClass psiClass, @NotNull List<PsiElement> result) {
    myConstructorProcessor.processElement(parent, psiClass, result);
    myToStringAnnotationProcessor.processElement(parent, psiClass, result);
    myEqualsAndHashCodeProcessor.processElement(parent, psiClass, result);
    List<PsiField> ownFields = LombokClassUtil.getOwnFields(psiClass);
    for(PsiField psiField : ownFields) {
      if(psiField.hasModifierProperty(PsiModifier.STATIC)) {
        continue;
      }

      if (!psiField.hasModifierProperty(PsiModifier.FINAL) &&
          AnnotationUtil.findAnnotation(psiField, LombokClassNames.LOMBOK_SETTER) == null) {
        mySetterProcessor.processElement(psiClass, psiField, result);
      }

      if(AnnotationUtil.findAnnotation(psiField, LombokClassNames.LOMBOK_GETTER) == null) {
        myGetterProcessor.processElement(psiClass, psiField, result);
      }
    }
  }

  @NotNull
  @Override
  public Class<? extends PsiElement> getCollectorPsiElementClass() {
    return PsiMethod.class;
  }
}