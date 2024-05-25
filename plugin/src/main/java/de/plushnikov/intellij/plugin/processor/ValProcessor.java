package de.plushnikov.intellij.plugin.processor;

import com.intellij.java.language.codeInsight.daemon.impl.analysis.JavaGenericsUtil;
import com.intellij.java.language.impl.psi.impl.source.JavaVarTypeUtil;
import com.intellij.java.language.psi.*;
import consulo.application.util.RecursionManager;
import consulo.language.editor.inspection.ProblemHighlightType;
import consulo.language.editor.inspection.ProblemsHolder;
import consulo.language.psi.PsiElement;
import consulo.util.lang.StringUtil;
import de.plushnikov.intellij.plugin.LombokBundle;
import de.plushnikov.intellij.plugin.LombokClassNames;
import de.plushnikov.intellij.plugin.problem.LombokProblem;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.Collection;
import java.util.Collections;

public class ValProcessor extends AbstractProcessor {

  private static final String LOMBOK_VAL_NAME = StringUtil.getShortName(LombokClassNames.VAL);
  private static final String LOMBOK_VAR_NAME = StringUtil.getShortName(LombokClassNames.VAR);

  public ValProcessor() {
    super(PsiElement.class, LombokClassNames.VAL, LombokClassNames.EXPERIMENTAL_VAR, LombokClassNames.VAR);
  }

  public static boolean isVal(@Nonnull PsiVariable psiVariable) {
    if (psiVariable instanceof PsiLocalVariable) {
      return isVal((PsiLocalVariable) psiVariable);
    }
    if (!(psiVariable instanceof PsiParameter psiParameter)) {
      return false;
    }
    PsiTypeElement typeElement = psiParameter.getTypeElement();
    if (typeElement == null) {
      return false;
    }
    return isPossibleVal(typeElement.getText()) && isVal(resolveQualifiedName(typeElement));
  }

  public static boolean isVar(@Nonnull PsiVariable psiVariable) {
    if (psiVariable instanceof PsiLocalVariable) {
      return isVar((PsiLocalVariable) psiVariable);
    }
    if (!(psiVariable instanceof PsiParameter psiParameter)) {
      return false;
    }
    PsiTypeElement typeElement = psiParameter.getTypeElement();
    if (typeElement == null) {
      return false;
    }
    return isPossibleVar(typeElement.getText()) && isVar(resolveQualifiedName(typeElement));
  }

  public static boolean isVal(@Nonnull PsiLocalVariable psiLocalVariable) {
    if (psiLocalVariable.hasInitializer()) {
      final PsiTypeElement typeElement = psiLocalVariable.getTypeElement();
      return isPossibleVal(typeElement.getText()) && isVal(resolveQualifiedName(typeElement));
    }
    return false;
  }

  public static boolean isVar(@Nonnull PsiLocalVariable psiLocalVariable) {
    if (psiLocalVariable.hasInitializer()) {
      final PsiTypeElement typeElement = psiLocalVariable.getTypeElement();
      return isPossibleVar(typeElement.getText()) && isVar(resolveQualifiedName(typeElement));
    }
    return false;
  }

  private static boolean isValOrVar(@Nonnull PsiLocalVariable psiLocalVariable) {
    if (psiLocalVariable.hasInitializer()) {
      final PsiTypeElement typeElement = psiLocalVariable.getTypeElement();
      return isPossibleValOrVar(typeElement.getText()) && isValOrVar(resolveQualifiedName(typeElement));
    }
    return false;
  }

  private static boolean isValOrVarForEach(@Nonnull PsiParameter psiParameter) {
    if (psiParameter.getParent() instanceof PsiForeachStatement) {
      final PsiTypeElement typeElement = psiParameter.getTypeElement();
      return null != typeElement && isPossibleValOrVar(typeElement.getText()) && isValOrVar(resolveQualifiedName(typeElement));
    }
    return false;
  }

  private static boolean isValOrVar(@Nullable String fullQualifiedName) {
    return isVal(fullQualifiedName) || isVar(fullQualifiedName);
  }

  private static boolean isPossibleValOrVar(@Nullable String shortName) {
    return isPossibleVal(shortName) || isPossibleVar(shortName);
  }

  private static boolean isPossibleVal(@Nullable String shortName) {
    return LOMBOK_VAL_NAME.equals(shortName);
  }

  private static boolean isVal(@Nullable String fullQualifiedName) {
    return LombokClassNames.VAL.equals(fullQualifiedName);
  }

  private static boolean isPossibleVar(@Nullable String shortName) {
    return LOMBOK_VAR_NAME.equals(shortName);
  }

  private static boolean isVar(@Nullable String fullQualifiedName) {
    return LombokClassNames.VAR.equals(fullQualifiedName) || LombokClassNames.EXPERIMENTAL_VAR.equals(fullQualifiedName);
  }

  @Nullable
  private static String resolveQualifiedName(@Nonnull PsiTypeElement typeElement) {
    PsiJavaCodeReferenceElement reference = typeElement.getInnermostComponentReferenceElement();
    if (reference == null) {
      return null;
    }

    return reference.getQualifiedName();
  }

  @Nonnull
  @Override
  public Collection<PsiAnnotation> collectProcessedAnnotations(@Nonnull PsiClass psiClass) {
    return Collections.emptyList();
  }

  @Nonnull
  @Override
  public Collection<LombokProblem> verifyAnnotation(@Nonnull PsiAnnotation psiAnnotation) {
    return Collections.emptyList();
  }

  public void verifyVariable(@Nonnull final PsiLocalVariable psiLocalVariable, @Nonnull final ProblemsHolder holder) {
    final PsiTypeElement typeElement = psiLocalVariable.getTypeElement();
    final String typeElementText = typeElement.getText();
    boolean isVal = isPossibleVal(typeElementText) && isVal(resolveQualifiedName(typeElement));
    boolean isVar = isPossibleVar(typeElementText) && isVar(resolveQualifiedName(typeElement));
    final String ann = isVal ? "val" : "var";
    if (isVal || isVar) {
      final PsiExpression initializer = psiLocalVariable.getInitializer();
      if (initializer == null) {
        holder.registerProblem(psiLocalVariable,
                               LombokBundle.message("inspection.message.on.local.variable.requires.initializer.expression", ann), ProblemHighlightType.ERROR);
      } else if (initializer instanceof PsiArrayInitializerExpression) {
        holder.registerProblem(psiLocalVariable,
                               LombokBundle.message("inspection.message.not.compatible.with.array.initializer.expressions", ann), ProblemHighlightType.ERROR);
      } else if (initializer instanceof PsiLambdaExpression) {
        holder.registerProblem(psiLocalVariable, LombokBundle.message("inspection.message.not.allowed.with.lambda.expressions", ann), ProblemHighlightType.ERROR);
      } else if (isVal) {
        final PsiElement typeParentParent = psiLocalVariable.getParent();
        if (typeParentParent instanceof PsiDeclarationStatement && typeParentParent.getParent() instanceof PsiForStatement) {
          holder.registerProblem(psiLocalVariable, LombokBundle.message("inspection.message.not.allowed.in.old.style.for.loops", ann), ProblemHighlightType.ERROR);
        }
      }
    }
  }

  public void verifyParameter(@Nonnull final PsiParameter psiParameter, @Nonnull final ProblemsHolder holder) {
    final PsiTypeElement typeElement = psiParameter.getTypeElement();
    final String typeElementText = null != typeElement ? typeElement.getText() : null;
    boolean isVal = isPossibleVal(typeElementText) && isVal(resolveQualifiedName(typeElement));
    boolean isVar = isPossibleVar(typeElementText) && isVar(resolveQualifiedName(typeElement));
    if (isVar || isVal) {
      PsiElement scope = psiParameter.getDeclarationScope();
      boolean isForeachStatement = scope instanceof PsiForeachStatement;
      boolean isForStatement = scope instanceof PsiForStatement;
      if (isVal && !isForeachStatement) {
        holder.registerProblem(psiParameter, LombokBundle.message("inspection.message.val.works.only.on.local.variables"), ProblemHighlightType.ERROR);
      } else if (isVar && !(isForeachStatement || isForStatement)) {
        holder.registerProblem(psiParameter,
                               LombokBundle.message("inspection.message.var.works.only.on.local.variables.on.for.foreach.loops"), ProblemHighlightType.ERROR);
      }
    }
  }

  public static boolean canInferType(@Nonnull PsiTypeElement typeElement) {
    final PsiElement parent = typeElement.getParent();
    return (parent instanceof PsiLocalVariable && isValOrVar((PsiLocalVariable) parent)) ||
      (parent instanceof PsiParameter && isValOrVarForEach((PsiParameter) parent));
  }

  @Nullable
  public static PsiType inferType(PsiTypeElement typeElement) {
    PsiType psiType = null;

    if (canInferType(typeElement)) {
      final PsiElement parent = typeElement.getParent();
      if (parent instanceof PsiLocalVariable) {
        psiType = processLocalVariableInitializer(((PsiLocalVariable) parent).getInitializer());
      } else {
        psiType = processParameterDeclaration(((PsiParameter) parent).getDeclarationScope());
      }

      if (null == psiType) {
        psiType = PsiType.getJavaLangObject(typeElement.getManager(), typeElement.getResolveScope());
      }
    }
    return psiType;
  }

  private static PsiType processLocalVariableInitializer(final PsiExpression psiExpression) {
    PsiType result = null;
    if (null != psiExpression && !(psiExpression instanceof PsiArrayInitializerExpression)) {
      result = RecursionManager.doPreventingRecursion(psiExpression, true, () -> {
        PsiType type = psiExpression.getType();
        // This is how IntelliJ resolves intersection types.
        // This way auto-completion won't show unavailable methods.
        if (type instanceof final PsiIntersectionType psiIntersectionType) {
          PsiType[] conjuncts = psiIntersectionType.getConjuncts();
          if (conjuncts.length > 0) {
            return conjuncts[0];
          }
        }
        if (type != null) {
          //Get upward projection, so you don't get types with missing diamonds.
          return JavaVarTypeUtil.getUpwardProjection(type);
        }
        return null;
      });
    }

    return result;
  }

  private static PsiType processParameterDeclaration(PsiElement parentDeclarationScope) {
    PsiType result = null;
    if (parentDeclarationScope instanceof PsiForeachStatement foreachStatement) {
      final PsiExpression iteratedValue = foreachStatement.getIteratedValue();
      if (iteratedValue != null) {
        result = JavaGenericsUtil.getCollectionItemType(iteratedValue);
      }
    }
    return result;
  }
}
