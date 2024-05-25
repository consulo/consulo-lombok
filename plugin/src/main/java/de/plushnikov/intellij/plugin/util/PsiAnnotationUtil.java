package de.plushnikov.intellij.plugin.util;

import com.intellij.java.language.codeInsight.AnnotationUtil;
import com.intellij.java.language.psi.*;
import consulo.language.psi.PsiElement;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.util.lang.StringUtil;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * Some util methods for annotation processing
 *
 * @author peichhorn
 * @author Plushnikov Michail
 */
public final class PsiAnnotationUtil {

  @Nonnull
  public static PsiAnnotation createPsiAnnotation(@Nonnull PsiModifierListOwner psiModifierListOwner, String annotationClassName) {
    return createPsiAnnotation(psiModifierListOwner, "", annotationClassName);
  }

  @Nonnull
  public static PsiAnnotation createPsiAnnotation(@Nonnull PsiModifierListOwner psiModifierListOwner,
                                                  @Nullable String value,
                                                  String annotationClassName) {
    final PsiElementFactory elementFactory = JavaPsiFacade.getElementFactory(psiModifierListOwner.getProject());
    final PsiClass psiClass = PsiTreeUtil.getParentOfType(psiModifierListOwner, PsiClass.class);
    final String valueString = StringUtil.isNotEmpty(value) ? "(" + value + ")" : "";
    return elementFactory.createAnnotationFromText("@" + annotationClassName + valueString, psiClass);
  }

  @Nonnull
  public static <T> Collection<T> getAnnotationValues(@Nonnull PsiAnnotation psiAnnotation, @Nonnull String parameter, @Nonnull Class<T> asClass) {
    Collection<T> result = Collections.emptyList();
    PsiAnnotationMemberValue attributeValue = psiAnnotation.findAttributeValue(parameter);
    if (attributeValue instanceof PsiArrayInitializerMemberValue) {
      final PsiAnnotationMemberValue[] memberValues = ((PsiArrayInitializerMemberValue) attributeValue).getInitializers();
      result = new ArrayList<>(memberValues.length);

      for (PsiAnnotationMemberValue memberValue : memberValues) {
        T value = resolveElementValue(memberValue, asClass);
        if (null != value) {
          result.add(value);
        }
      }
    } else if (null != attributeValue) {
      T value = resolveElementValue(attributeValue, asClass);
      if (null != value) {
        result = Collections.singletonList(value);
      }
    }
    return result;
  }

  public static boolean hasDeclaredProperty(@Nonnull PsiAnnotation psiAnnotation, @Nonnull String parameter) {
    return null != psiAnnotation.findDeclaredAttributeValue(parameter);
  }

  public static boolean getBooleanAnnotationValue(@Nonnull PsiAnnotation psiAnnotation, @Nonnull String parameter, boolean defaultValue) {
    final Boolean result = psiAnnotation.findAttributeValue(parameter) != null ? AnnotationUtil.getBooleanAttributeValue(psiAnnotation, parameter) : null;
    return result == null ? defaultValue : result;
  }

  public static String getStringAnnotationValue(@Nonnull PsiAnnotation psiAnnotation, @Nonnull String parameter, @Nonnull String defaultValue) {
    final String result = AnnotationUtil.getDeclaredStringAttributeValue(psiAnnotation, parameter);
    return result != null ? result : defaultValue;
  }

  public static String getEnumAnnotationValue(@Nonnull PsiAnnotation psiAnnotation, @Nonnull String attributeName, @Nonnull String defaultValue) {
    PsiAnnotationMemberValue attrValue = psiAnnotation.findDeclaredAttributeValue(attributeName);
    String result = attrValue != null ? resolveElementValue(attrValue, String.class) : null;
    return result != null ? result : defaultValue;
  }

  public static int getIntAnnotationValue(@Nonnull PsiAnnotation psiAnnotation, @Nonnull String attributeName, int defaultValue) {
    PsiAnnotationMemberValue attrValue = psiAnnotation.findDeclaredAttributeValue(attributeName);
    PsiConstantEvaluationHelper evaluationHelper = JavaPsiFacade.getInstance(psiAnnotation.getProject()).getConstantEvaluationHelper();
    Object result = evaluationHelper.computeConstantExpression(attrValue);
    return result instanceof Number ? ((Number) result).intValue() : defaultValue;
  }

  @Nullable
  private static <T> T resolveElementValue(@Nonnull PsiElement psiElement, @Nonnull Class<T> asClass) {
    T value = null;
    if (psiElement instanceof PsiReferenceExpression) {
      final PsiElement resolved = ((PsiReferenceExpression) psiElement).resolve();

      if (resolved instanceof PsiEnumConstant psiEnumConstant) {
        //Enums are supported as VALUE-Strings only
        if (asClass.isAssignableFrom(String.class)) {
          value = (T) psiEnumConstant.getName();
        }
      } else if (resolved instanceof PsiVariable psiVariable) {
        Object elementValue = psiVariable.computeConstantValue();
        if (null != elementValue && asClass.isAssignableFrom(elementValue.getClass())) {
          value = (T) elementValue;
        }
      }
    } else if (psiElement instanceof PsiLiteralExpression) {
      Object elementValue = ((PsiLiteralExpression) psiElement).getValue();
      if (null != elementValue && asClass.isAssignableFrom(elementValue.getClass())) {
        value = (T) elementValue;
      }
    } else if (psiElement instanceof PsiClassObjectAccessExpression) {
      PsiTypeElement elementValue = ((PsiClassObjectAccessExpression) psiElement).getOperand();
      //Enums are supported as VALUE-Strings only
      if (asClass.isAssignableFrom(PsiType.class)) {
        value = (T) elementValue.getType();
      }
    } else if (psiElement instanceof PsiAnnotation) {
      if (asClass.isAssignableFrom(PsiAnnotation.class)) {
        value = (T) psiElement;
      }
    } else if (psiElement instanceof PsiPrefixExpression) {
      if (asClass.isAssignableFrom(String.class)) {
        String expressionText = psiElement.getText();
        value = (T) expressionText;
      }
    }
    return value;
  }

  @Nullable
  public static Boolean getDeclaredBooleanAnnotationValue(@Nonnull PsiAnnotation psiAnnotation, @Nonnull String parameter) {
    PsiAnnotationMemberValue attributeValue = psiAnnotation.findDeclaredAttributeValue(parameter);
    final JavaPsiFacade javaPsiFacade = JavaPsiFacade.getInstance(psiAnnotation.getProject());
    Object constValue = javaPsiFacade.getConstantEvaluationHelper().computeConstantExpression(attributeValue);
    return constValue instanceof Boolean ? (Boolean) constValue : null;
  }
}
