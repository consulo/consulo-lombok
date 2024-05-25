package de.plushnikov.intellij.plugin.intention.valvar.to;

import com.intellij.java.analysis.impl.codeInspection.RemoveRedundantTypeArgumentsUtil;
import com.intellij.java.impl.refactoring.IntroduceVariableUtil;
import com.intellij.java.language.psi.*;
import com.intellij.java.language.psi.util.PsiUtil;
import consulo.language.codeStyle.CodeStyleManager;
import consulo.language.psi.PsiElement;
import consulo.project.Project;
import consulo.util.lang.StringUtil;
import de.plushnikov.intellij.plugin.LombokBundle;
import de.plushnikov.intellij.plugin.intention.valvar.AbstractValVarIntentionAction;
import org.jetbrains.annotations.Nls;
import jakarta.annotation.Nonnull;

public abstract class AbstractReplaceExplicitTypeWithVariableIntentionAction extends AbstractValVarIntentionAction {

  private final String variableClassName;

  public AbstractReplaceExplicitTypeWithVariableIntentionAction(String variableClassName) {
    this.variableClassName = variableClassName;
  }

  @Nls(capitalization = Nls.Capitalization.Sentence)
  @Override
  public String getText() {
    return LombokBundle.message("replace.explicit.type.with.0.lombok", StringUtil.getShortName(variableClassName));
  }

  @Override
  public boolean isAvailableOnDeclarationStatement(PsiDeclarationStatement context) {
    if (PsiUtil.isLanguageLevel10OrHigher(context)) {
      return false;
    }
    PsiElement[] declaredElements = context.getDeclaredElements();
    if (declaredElements.length != 1) {
      return false;
    }
    PsiElement declaredElement = declaredElements[0];
    if (!(declaredElement instanceof PsiLocalVariable localVariable)) {
      return false;
    }
    if (!localVariable.hasInitializer()) {
      return false;
    }
    PsiExpression initializer = localVariable.getInitializer();
    if (initializer instanceof PsiArrayInitializerExpression || initializer instanceof PsiLambdaExpression) {
      return false;
    }
    if (localVariable.getTypeElement().isInferredType()) {
      return false;
    }
    return isAvailableOnDeclarationCustom(context, localVariable);
  }

  protected abstract boolean isAvailableOnDeclarationCustom(@Nonnull PsiDeclarationStatement context, @Nonnull PsiLocalVariable localVariable);

  @Override
  public void invokeOnDeclarationStatement(PsiDeclarationStatement declarationStatement) {
    if (declarationStatement.getDeclaredElements().length == 1) {
      PsiLocalVariable localVariable = (PsiLocalVariable) declarationStatement.getDeclaredElements()[0];
      invokeOnVariable(localVariable);
    }
  }

  @Override
  public void invokeOnVariable(PsiVariable psiVariable) {
    Project project = psiVariable.getProject();
    psiVariable.normalizeDeclaration();
    PsiTypeElement typeElement = psiVariable.getTypeElement();
    if (typeElement == null || typeElement.isInferredType()) {
      return;
    }

    PsiElementFactory elementFactory = JavaPsiFacade.getElementFactory(project);
    PsiClass variablePsiClass = JavaPsiFacade.getInstance(project).findClass(variableClassName, psiVariable.getResolveScope());
    if (variablePsiClass == null) {
      return;
    }
    PsiJavaCodeReferenceElement referenceElementByFQClassName = elementFactory.createReferenceElementByFQClassName(variableClassName, psiVariable.getResolveScope());
    typeElement = (PsiTypeElement) IntroduceVariableUtil.expandDiamondsAndReplaceExplicitTypeWithVar(typeElement, typeElement);
    typeElement.deleteChildRange(typeElement.getFirstChild(), typeElement.getLastChild());
    typeElement.add(referenceElementByFQClassName);
    RemoveRedundantTypeArgumentsUtil.removeRedundantTypeArguments(psiVariable);
    executeAfterReplacing(psiVariable);
    CodeStyleManager.getInstance(project).reformat(psiVariable);
  }

  protected abstract void executeAfterReplacing(PsiVariable psiVariable);
}
