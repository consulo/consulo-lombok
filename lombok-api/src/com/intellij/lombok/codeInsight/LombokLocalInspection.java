package com.intellij.lombok.codeInsight;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.lombok.processors.LombokProcessor;
import com.intellij.lombok.processors.LombokProcessorEP;
import com.intellij.psi.JavaElementVisitor;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;

/**
 * @author VISTALL
 * @since 19:34/02.05.13
 */
public class LombokLocalInspection extends LocalInspectionTool {
  @NotNull
  @Override
  public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
    return new JavaElementVisitor() {
      @Override
      public void visitClass(PsiClass aClass) {
        for(LombokProcessorEP ep : LombokProcessorEP.EP_NAME.getExtensions()) {
          LombokProcessor instance = ep.getInstance();

          instance.collectInspections(aClass, holder);
        }
      }
    };
  }
}
