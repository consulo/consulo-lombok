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
package consulo.lombok.impl.psi.impl.source;

import com.intellij.java.language.impl.psi.impl.java.stubs.PsiModifierListStub;
import com.intellij.java.language.impl.psi.impl.source.PsiModifierListImpl;
import consulo.language.ast.ASTNode;
import consulo.language.psi.PsiElement;
import consulo.lombok.impl.module.extension.LombokModuleExtension;
import consulo.lombok.impl.psi.LombokElementWithAdditionalModifiers;
import consulo.lombok.processors.util.LombokUtil;
import consulo.util.collection.ArrayUtil;
import jakarta.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 17:54/30.03.13
 */
public class LombokPsiModifierListImpl extends PsiModifierListImpl
{
  public LombokPsiModifierListImpl(PsiModifierListStub stub) {
    super(stub);
  }

  public LombokPsiModifierListImpl(ASTNode node) {
    super(node);
  }

  @Override
  public boolean hasModifierProperty(@Nonnull String name) {
    boolean val = super.hasModifierProperty(name);
    if(val) {
      return true;
    }

    if(!LombokUtil.isExtensionEnabled(this, LombokModuleExtension.class)) {
      return false;
    }

    final PsiElement parent = getParent();

    if(parent instanceof LombokElementWithAdditionalModifiers) {
      return ArrayUtil.contains(name, ((LombokElementWithAdditionalModifiers)parent).getAdditionalModifiers());
    }
    return false;
  }
}
