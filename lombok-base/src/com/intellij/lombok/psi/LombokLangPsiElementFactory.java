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
package com.intellij.lombok.psi;

import com.intellij.lang.ASTNode;
import com.intellij.lang.LangPsiElementFactory;
import com.intellij.lombok.psi.impl.source.LombokPsiModifierListImpl;
import com.intellij.lombok.psi.impl.source.LombokPsiParameterImpl;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.java.stubs.JavaStubElementTypes;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author VISTALL
 * @since 17:43/30.03.13
 */
public class LombokLangPsiElementFactory implements LangPsiElementFactory {
  @Nullable
  @Override
  public PsiElement createElement(@NotNull ASTNode node) {
    final IElementType elementType = node.getElementType();
    if(elementType == JavaStubElementTypes.PARAMETER) {
      return new LombokPsiParameterImpl(node);
    }
    else if(elementType == JavaStubElementTypes.MODIFIER_LIST) {
      return new LombokPsiModifierListImpl(node);
    }
    return null;
  }

  @Override
  public boolean apply(@NotNull IElementType type) {
    return type == JavaStubElementTypes.PARAMETER || type == JavaStubElementTypes.MODIFIER_LIST;
  }
}
