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
package consulo.lombok.processors.util;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

import com.intellij.psi.PsiAnnotationMemberValue;
import com.intellij.psi.PsiArrayInitializerMemberValue;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiClassObjectAccessExpression;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifier;
import com.intellij.psi.PsiType;
import com.intellij.psi.PsiTypeElement;
import com.intellij.psi.impl.source.PsiExtensibleClass;
import com.intellij.psi.util.PsiTypesUtil;
import com.intellij.util.containers.MultiMap;

/**
 * @author VISTALL
 * @since 22:47/30.03.13
 */
public class LombokClassUtil {
  public static List<PsiMethod> getOwnMethods(PsiClass psiClass) {
    if (psiClass instanceof PsiExtensibleClass) {
      return ((PsiExtensibleClass)psiClass).getOwnMethods();
    }
    else {
      return Collections.emptyList();
    }
  }

  public static List<PsiField> getOwnFields(PsiClass psiClass) {
    if (psiClass instanceof PsiExtensibleClass) {
      return ((PsiExtensibleClass)psiClass).getOwnFields();
    }
    else {
      return Collections.emptyList();
    }
  }

  @Nonnull
  public static MultiMap<PsiClass, PsiMethod> collectMethods(@Nonnull PsiClass parent, @Nonnull PsiAnnotationMemberValue value) {
    return collectMethods(parent, value, new MultiMap<PsiClass, PsiMethod>());
  }

  @Nonnull
  public static MultiMap<PsiClass, PsiMethod> collectMethodsOfClass(PsiClass parent, PsiType type) {
    MultiMap<PsiClass, PsiMethod> map = new MultiMap<PsiClass, PsiMethod>();
    collectMethodsOfClass(parent, map, type);
    return map;
  }

  private static MultiMap<PsiClass, PsiMethod> collectMethods(@Nonnull PsiClass parent,
                                                              @Nonnull PsiAnnotationMemberValue value,
                                                              MultiMap<PsiClass, PsiMethod> methods) {
    if (value instanceof PsiArrayInitializerMemberValue) {
      final PsiAnnotationMemberValue[] initializers = ((PsiArrayInitializerMemberValue)value).getInitializers();
      for (PsiAnnotationMemberValue initializer : initializers) {
        collectMethods(parent, initializer, methods);
      }
    }
    else if (value instanceof PsiClassObjectAccessExpression) {
      final PsiTypeElement operand = ((PsiClassObjectAccessExpression)value).getOperand();
      final PsiType type = operand.getType();

      collectMethodsOfClass(parent, methods, type);
    }
    return methods;
  }

  public static void collectMethodsOfClass(PsiClass parent, MultiMap<PsiClass, PsiMethod> methods, PsiType type) {
    PsiClass psiClass = PsiTypesUtil.getPsiClass(type);
    if (psiClass != null) {
      // this is need check, if not it ill be stackoverflow
      PsiMethod[] temp = psiClass == parent ? PsiMethod.EMPTY_ARRAY : psiClass.getAllMethods();
      for (PsiMethod method : temp) {
        if (method.hasModifierProperty(PsiModifier.STATIC) || method.hasModifierProperty(PsiModifier.PRIVATE)) {
          continue;
        }
        methods.putValue(psiClass, method);
      }
    }
  }
}
