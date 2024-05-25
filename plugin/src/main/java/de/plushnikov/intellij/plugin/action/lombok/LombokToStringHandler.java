package de.plushnikov.intellij.plugin.action.lombok;

import com.intellij.java.language.psi.*;
import de.plushnikov.intellij.plugin.LombokClassNames;
import jakarta.annotation.Nonnull;

public class LombokToStringHandler extends BaseLombokHandler {

  @Override
  protected void processClass(@Nonnull PsiClass psiClass) {
    final PsiElementFactory factory = JavaPsiFacade.getElementFactory(psiClass.getProject());
    final PsiClassType stringClassType = factory.createTypeByFQClassName(CommonClassNames.JAVA_LANG_STRING, psiClass.getResolveScope());

    final PsiMethod toStringMethod = findPublicNonStaticMethod(psiClass, "toString", stringClassType);
    if (null != toStringMethod) {
      toStringMethod.delete();
    }
    addAnnotation(psiClass, LombokClassNames.TO_STRING);
  }

}
