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
package org.consulo.lombok.processors.util;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.psi.*;
import com.intellij.psi.impl.light.LightMethodBuilder;
import org.consulo.lombok.module.extension.LombokModuleExtension;
import org.jetbrains.annotations.NotNull;

/**
 * @author VISTALL
 * @since 19:45/29.03.13
 */
public class LombokUtil {
  public static boolean isLombokExtensionEnabled(@NotNull PsiElement element) {
    Module moduleForPsiElement = ModuleUtilCore.findModuleForPsiElement(element);
    if(moduleForPsiElement == null) {
      return false;
    }
    return ModuleUtil.getExtension(moduleForPsiElement, LombokModuleExtension.class) != null;
  }

  public static void copyAccessModifierFromOriginal(PsiModifierListOwner from, LightMethodBuilder to) {
    if (from.hasModifierProperty(PsiModifier.PRIVATE)) {
      to.addModifier(PsiModifier.PRIVATE);
    }
    else if (from.hasModifierProperty(PsiModifier.PUBLIC)) {
      to.addModifier(PsiModifier.PUBLIC);
    }
    else if (from.hasModifierProperty(PsiModifier.PROTECTED)) {
      to.addModifier(PsiModifier.PROTECTED);
    }
  }

  public static void setAccessModifierFromAnnotation(@NotNull PsiAnnotation annotation, LightMethodBuilder to, String methodName) {
    String modifier = getModifierFromAnnotation(annotation, methodName);
    to.addModifier(modifier);
  }

  @NotNull
  public static String getModifierFromAnnotation(@NotNull PsiAnnotation annotation, String methodName) {
    final PsiAnnotationMemberValue attributeValue = annotation.findAttributeValue(methodName);
    if (attributeValue instanceof PsiReference) {
      final PsiElement resolve = ((PsiReference)attributeValue).resolve();
      if (resolve instanceof PsiEnumConstant) {
        final String name = ((PsiEnumConstant)resolve).getName();

        if (name.equals("PUBLIC")) {
          return PsiModifier.PUBLIC;
        }
        else if (name.equals("PRIVATE")) {
          return PsiModifier.PRIVATE;
        }
        else if (name.equals("PROTECTED")) {
          return PsiModifier.PROTECTED;
        }
        else if (name.equals("PACKAGE")) {
          return PsiModifier.PACKAGE_LOCAL;
        }

        // FIXME [VISTALL] MODULE & NONE modifier
      }
    }
    return PsiModifier.PUBLIC;
  }
}
