package com.intellij.lombok.codeInsight.typeCanBeReplacedByVal;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.lombok.LombokClassNames;
import com.intellij.lombok.psi.impl.source.LombokValOwner;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
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
        if (typeElement == null) {
          return;
        }
        holder.registerProblem(typeElement, "Type can replaced by 'lombok.val'", new MyLocalQuickFix(variable, typeElement));
      }
    }
  }
}
