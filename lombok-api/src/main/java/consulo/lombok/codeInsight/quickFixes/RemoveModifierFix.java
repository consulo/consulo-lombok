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

import org.jetbrains.annotations.NotNull;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiModifier;
import com.intellij.psi.PsiModifierList;
import com.intellij.psi.PsiModifierListOwner;

/**
 * @author VISTALL
 * @since 20:02/02.05.13
 */
public class RemoveModifierFix implements LocalQuickFix {
  private final PsiModifierListOwner myModifierListOwner;
  @PsiModifier.ModifierConstant
  private final String myModifier;

  public RemoveModifierFix(@NotNull PsiModifierListOwner modifierListOwner, @PsiModifier.ModifierConstant String modifier) {
    myModifierListOwner = modifierListOwner;
    myModifier = modifier;
  }

  @NotNull
  @Override
  public String getName() {
    return String.format("Remove '%s' modifier", myModifier);
  }

  @NotNull
  @Override
  public String getFamilyName() {
    return "Lombok";
  }

  @Override
  public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
    PsiModifierList modifierList = myModifierListOwner.getModifierList();
    if(modifierList == null) {
      return;
    }
    modifierList.setModifierProperty(myModifier, false);
  }
}
