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
package consulo.lombok.psi;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.java.stubs.JavaStubElementTypes;
import com.intellij.psi.tree.IElementType;
import consulo.lombok.psi.impl.source.LombokPsiModifierListImpl;
import consulo.lombok.psi.impl.source.LombokPsiParameterImpl;
import consulo.psi.tree.PsiElementFactory;

/**
 * @author VISTALL
 * @since 17:43/30.03.13
 */
public class LombokPsiElementFactory implements PsiElementFactory
{
  @Nullable
  @Override
  public PsiElement createElement(@Nonnull ASTNode node) {
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
  public boolean apply(@Nonnull IElementType type) {
    return type == JavaStubElementTypes.PARAMETER || type == JavaStubElementTypes.MODIFIER_LIST;
  }
}
