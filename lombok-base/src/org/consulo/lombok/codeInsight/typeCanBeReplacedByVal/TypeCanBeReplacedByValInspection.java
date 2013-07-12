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

package org.consulo.lombok.codeInsight.typeCanBeReplacedByVal;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import org.consulo.lombok.LombokClassNames;
import org.consulo.lombok.processors.util.LombokUtil;
import org.consulo.lombok.psi.impl.source.LombokValOwner;
import org.jetbrains.annotations.NotNull;

/**
 * @author VISTALL
 * @since 20:13/02.05.13
 */
public class TypeCanBeReplacedByValInspection extends LocalInspectionTool {
  private static class MyLocalQuickFix implements LocalQuickFix {
    private final PsiVariable myVariable;
    private final PsiTypeElement myTypeElement;

    public MyLocalQuickFix(PsiVariable variable, PsiTypeElement typeElement) {
      myVariable = variable;
      myTypeElement = typeElement;
    }

    @NotNull
    @Override
    public String getName() {
      return "Replace type by 'lombok.val'";
    }

    @NotNull
    @Override
    public String getFamilyName() {
      return "Lombok";
    }

    @Override
    public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
      PsiTypeElement valTypeElement = JavaPsiFacade.getElementFactory(myVariable.getProject())
        .createTypeElementFromText(LombokClassNames.LOMBOK_VAL, myVariable);

      myTypeElement.replace(valTypeElement);
    }
  }

  @NotNull
  @Override
  public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
    if(!LombokUtil.isExtensionEnabled(holder.getFile())) {
      return new JavaElementVisitor() {};
    }

    return new JavaElementVisitor() {
      @Override
      public void visitForeachStatement(PsiForeachStatement statement) {
        final PsiParameter iterationParameter = statement.getIterationParameter();

        registerProblem(iterationParameter, holder);
      }

      @Override
      public void visitLocalVariable(PsiLocalVariable variable) {
        registerProblem(variable, holder);
      }
    };
  }

  private static void registerProblem(@NotNull PsiVariable variable, @NotNull final ProblemsHolder holder) {
    if(variable instanceof LombokValOwner) {
      PsiType rightTypeIfCan = ((LombokValOwner)variable).findRightTypeIfCan();
      if (rightTypeIfCan == null) {
        final PsiTypeElement typeElement = variable.getTypeElement();
        if (typeElement == null || !variable.hasModifierProperty(PsiModifier.FINAL)) {
          return;
        }
        holder.registerProblem(typeElement, "Type can replaced by 'lombok.val'", new MyLocalQuickFix(variable, typeElement));
      }
    }
  }
}
