package com.intellij.lombok.processors;

import com.intellij.codeInsight.AnnotationUtil;
import com.intellij.lombok.LombokClassNames;
import com.intellij.lombok.processors.util.LombokClassUtil;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * @author VISTALL
 * @since 21:00/30.04.13
 */
public class DataAnnotationProcessor extends LombokSelfClassProcessor {
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
