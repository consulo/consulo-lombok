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
package consulo.lombok.impl.psi;

import com.intellij.java.language.impl.psi.impl.source.tree.JavaElementType;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.ast.IElementType;
import consulo.language.impl.ast.ASTCompositeFactory;
import consulo.language.impl.ast.CompositeElement;
import consulo.lombok.impl.psi.impl.source.LombokPsiLocalVariableImpl;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * @author VISTALL
 * @since 19:10/30.03.13
 */
@ExtensionImpl
public class LombokASTCompositeFactory implements ASTCompositeFactory
{
  @Override
  @Nonnull
  public CompositeElement createComposite(IElementType type) {
    return new LombokPsiLocalVariableImpl();
  }

  @Override
  public boolean test(@Nullable IElementType input) {
    return input == JavaElementType.LOCAL_VARIABLE;
  }
}
