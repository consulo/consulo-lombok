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

package consulo.lombok.codeInsight.quickFixes;

import com.intellij.java.language.psi.PsiModifier;
import com.intellij.java.language.psi.PsiModifierList;
import com.intellij.java.language.psi.PsiModifierListOwner;
import consulo.language.editor.inspection.LocalQuickFix;
import consulo.language.editor.inspection.ProblemDescriptor;
import consulo.project.Project;

import jakarta.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 20:02/02.05.13
 */
public class RemoveModifierFix implements LocalQuickFix
{
  private final PsiModifierListOwner myModifierListOwner;
  @PsiModifier.ModifierConstant
  private final String myModifier;

  public RemoveModifierFix(@Nonnull PsiModifierListOwner modifierListOwner, @PsiModifier.ModifierConstant String modifier) {
    myModifierListOwner = modifierListOwner;
    myModifier = modifier;
  }

  @Nonnull
  @Override
  public String getName() {
    return String.format("Remove '%s' modifier", myModifier);
  }

  @Nonnull
  @Override
  public String getFamilyName() {
    return "Lombok";
  }

  @Override
  public void applyFix(@Nonnull Project project, @Nonnull ProblemDescriptor descriptor) {
    PsiModifierList modifierList = myModifierListOwner.getModifierList();
    if(modifierList == null) {
      return;
    }
    modifierList.setModifierProperty(myModifier, false);
  }
}
