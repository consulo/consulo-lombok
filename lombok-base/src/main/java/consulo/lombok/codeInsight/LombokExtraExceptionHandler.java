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
package consulo.lombok.codeInsight;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import com.intellij.codeInsight.AnnotationUtil;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import consulo.java.codeInsight.ExtraExceptionHandler;
import consulo.lombok.LombokClassNames;
import consulo.lombok.module.extension.LombokModuleExtension;
import consulo.lombok.processors.util.LombokUtil;

/**
 * @author VISTALL
 * @since 23:49/30.03.13
 */
public class LombokExtraExceptionHandler implements ExtraExceptionHandler
{
  @Override
  public boolean isHandled(@NotNull PsiClassType type, @NotNull PsiElement element) {
    if(!LombokUtil.isExtensionEnabled(element, LombokModuleExtension.class)) {
      return false;
    }
    PsiMethod parent = PsiTreeUtil.getParentOfType(element, PsiMethod.class);
    if (parent == null) {
      return false;
    }

    final PsiAnnotation annotation = AnnotationUtil.findAnnotation(parent, LombokClassNames.LOMBOK_SNEAKY_THROWS);
    if (annotation == null) {
      return false;
    }

    final PsiAnnotationMemberValue attributeValue = annotation.findAttributeValue(PsiAnnotation.DEFAULT_REFERENCED_METHOD_NAME);
    if(attributeValue == null) {
      return false;
    }
    List<PsiClass> classes = new ArrayList<PsiClass>();
    collectClassesFromAnnotationValue(attributeValue, classes);
    if(classes.isEmpty()) {
      return true;
    }
    else {
      PsiClass target = type.resolve();
      if(target == null) {
        return false;
      }

      for(PsiClass psiClass : classes) {
       if(psiClass == target || target.isInheritor(psiClass, true)) {
         return true;
       }
      }
    }
    return false;
  }

  private static void collectClassesFromAnnotationValue(@NotNull PsiAnnotationMemberValue value, List<PsiClass> classes) {
    if(value instanceof PsiArrayInitializerMemberValue) {
      final PsiAnnotationMemberValue[] initializers = ((PsiArrayInitializerMemberValue)value).getInitializers();
      for(PsiAnnotationMemberValue initializer : initializers) {
        collectClassesFromAnnotationValue(initializer, classes);
      }
    }
    else if(value instanceof PsiClassObjectAccessExpression) {
      final PsiTypeElement operand = ((PsiClassObjectAccessExpression)value).getOperand();
      final PsiType type = operand.getType();
      if(type instanceof PsiClassType) {
        final PsiClass resolve = ((PsiClassType)type).resolve();
        if(resolve != null) {
          classes.add(resolve);
        }
      }
    }
  }
}
