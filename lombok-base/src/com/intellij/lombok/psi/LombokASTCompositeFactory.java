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

import com.intellij.lang.ASTCompositeFactory;
import com.intellij.lombok.psi.impl.source.LombokPsiLocalVariableImpl;
import com.intellij.psi.impl.source.tree.CompositeElement;
import com.intellij.psi.impl.source.tree.JavaElementType;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author VISTALL
 * @since 19:10/30.03.13
 */
public class LombokASTCompositeFactory implements ASTCompositeFactory {
  @Override
  @NotNull
  public CompositeElement createComposite(IElementType type) {
    return new LombokPsiLocalVariableImpl();
  }

  @Override
  public boolean apply(@Nullable IElementType input) {
    return input == JavaElementType.LOCAL_VARIABLE;
  }
}
