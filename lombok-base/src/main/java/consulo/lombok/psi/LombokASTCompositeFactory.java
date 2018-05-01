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

import com.intellij.psi.impl.source.tree.CompositeElement;
import com.intellij.psi.impl.source.tree.JavaElementType;
import com.intellij.psi.tree.IElementType;
import consulo.lombok.psi.impl.source.LombokPsiLocalVariableImpl;
import consulo.psi.tree.ASTCompositeFactory;

/**
 * @author VISTALL
 * @since 19:10/30.03.13
 */
public class LombokASTCompositeFactory implements ASTCompositeFactory
{
  @Override
  @Nonnull
  public CompositeElement createComposite(IElementType type) {
    return new LombokPsiLocalVariableImpl();
  }

  @Override
  public boolean apply(@Nullable IElementType input) {
    return input == JavaElementType.LOCAL_VARIABLE;
  }
}
