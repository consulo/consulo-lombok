/*
 * Copyright 2000-2013 JetBrains s.r.o.
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
package com.intellij.lombok.psi.impl.source;

import com.intellij.lombok.LombokClassNames;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiExpression;
import com.intellij.psi.PsiModifier;
import com.intellij.psi.PsiType;
import com.intellij.psi.impl.source.tree.java.PsiLocalVariableImpl;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTypesUtil;
import com.intellij.util.ArrayUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author VISTALL
 * @since 18:08/30.03.13
 */
public class LombokPsiLocalVariableImpl extends PsiLocalVariableImpl implements LombokValOwner {
  private static final String[] ourAdditionalModifiers = new String[]{PsiModifier.FINAL};

  public LombokPsiLocalVariableImpl() {
    this(LOCAL_VARIABLE);
  }

  protected LombokPsiLocalVariableImpl(final IElementType type) {
    super(type);
  }

  @NotNull
  @Override
  public PsiType getType() {
    final PsiType type = findRightTypeIfCan();
    return type == null ? super.getType() : type;
  }

  @Override
  @Nullable
  public PsiType findRightTypeIfCan() {
    final PsiClass resolve = PsiTypesUtil.getPsiClass(super.getType());
    if (resolve == null || !LombokClassNames.LOMBOK_VAL.equals(resolve.getQualifiedName())) {
      return null;
    }
    final PsiExpression expression = getInitializer();
    if (expression == null) {
      return null;
    }
    final PsiType expressionType = expression.getType();
    return expressionType == null ? null : expressionType;
  }

  @Override
  public String[] getAdditionalModifiers() {
    return findRightTypeIfCan() == null ? ArrayUtil.EMPTY_STRING_ARRAY : ourAdditionalModifiers;
  }
}
