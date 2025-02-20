package de.plushnikov.intellij.plugin.processor.method;

import com.intellij.java.language.psi.PsiAnnotation;
import com.intellij.java.language.psi.PsiMethod;
import com.intellij.java.language.psi.PsiType;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.psi.PsiElement;
import de.plushnikov.intellij.plugin.LombokClassNames;
import de.plushnikov.intellij.plugin.problem.ProblemSink;
import de.plushnikov.intellij.plugin.processor.handler.DelegateHandler;
import jakarta.annotation.Nonnull;

import java.util.List;

@ExtensionImpl(id = "DelegateMethodProcessor", order = "after WitherFieldProcessor")
public class DelegateMethodProcessor extends AbstractMethodProcessor {

  public DelegateMethodProcessor() {
    super(PsiMethod.class, LombokClassNames.DELEGATE, LombokClassNames.EXPERIMENTAL_DELEGATE);
  }

  @Override
  protected boolean validate(@Nonnull PsiAnnotation psiAnnotation, @Nonnull PsiMethod psiMethod, @Nonnull ProblemSink problemSink) {
    boolean result = true;
    if (psiMethod.hasParameters()) {
      problemSink.addErrorMessage("inspection.message.delegate.legal.only.on.no.argument.methods");
      result = false;
    }

    final PsiType returnType = psiMethod.getReturnType();
    result &= null != returnType && DelegateHandler.validate(psiMethod, returnType, psiAnnotation, problemSink);

    return result;
  }

  @Override
  protected void processIntern(@Nonnull PsiMethod psiMethod, @Nonnull PsiAnnotation psiAnnotation, @Nonnull List<? super PsiElement> target) {
    final PsiType returnType = psiMethod.getReturnType();
    if (null != returnType) {
      DelegateHandler.generateElements(psiMethod, returnType, psiAnnotation, target);
    }
  }
}
