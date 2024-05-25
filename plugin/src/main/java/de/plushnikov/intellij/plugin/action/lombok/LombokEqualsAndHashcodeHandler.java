package de.plushnikov.intellij.plugin.action.lombok;

import com.intellij.java.language.psi.PsiClass;
import com.intellij.java.language.psi.PsiMethod;
import com.intellij.java.language.psi.PsiType;
import com.intellij.java.language.psi.PsiTypes;
import de.plushnikov.intellij.plugin.LombokClassNames;
import jakarta.annotation.Nonnull;

public class LombokEqualsAndHashcodeHandler extends BaseLombokHandler {

  @Override
  protected void processClass(@Nonnull PsiClass psiClass) {
    final PsiMethod equalsMethod = findPublicNonStaticMethod(psiClass, "equals", PsiTypes.booleanType(),
                                                             PsiType.getJavaLangObject(psiClass.getManager(), psiClass.getResolveScope()));
    if (null != equalsMethod) {
      equalsMethod.delete();
    }

    final PsiMethod hashCodeMethod = findPublicNonStaticMethod(psiClass, "hashCode", PsiTypes.intType());
    if (null != hashCodeMethod) {
      hashCodeMethod.delete();
    }

    addAnnotation(psiClass, LombokClassNames.EQUALS_AND_HASHCODE);
  }
}
