package de.plushnikov.intellij.plugin.action.lombok;

import com.intellij.java.language.psi.PsiClass;
import com.intellij.java.language.psi.PsiField;
import com.intellij.java.language.psi.PsiMethod;
import com.intellij.java.language.psi.PsiModifier;
import com.intellij.java.language.psi.util.PropertyUtilBase;
import de.plushnikov.intellij.plugin.LombokClassNames;
import jakarta.annotation.Nonnull;

import java.util.HashMap;
import java.util.Map;

public class LombokGetterHandler extends BaseLombokHandler {

  @Override
  protected void processClass(@Nonnull PsiClass psiClass) {
    final Map<PsiField, PsiMethod> fieldMethodMap = new HashMap<>();
    for (PsiField psiField : psiClass.getFields()) {
      PsiMethod propertyGetter =
        PropertyUtilBase.findPropertyGetter(psiClass, psiField.getName(), psiField.hasModifierProperty(PsiModifier.STATIC), false);

      if (null != propertyGetter) {
        fieldMethodMap.put(psiField, propertyGetter);
      }
    }

    processIntern(fieldMethodMap, psiClass, LombokClassNames.GETTER);
  }

}
