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

public class LombokSetterHandler extends BaseLombokHandler {

  @Override
  protected void processClass(@Nonnull PsiClass psiClass) {
    final Map<PsiField, PsiMethod> fieldMethodMap = new HashMap<>();
    for (PsiField psiField : psiClass.getFields()) {
      PsiMethod propertySetter =
        PropertyUtilBase.findPropertySetter(psiClass, psiField.getName(), psiField.hasModifierProperty(PsiModifier.STATIC), false);

      if (null != propertySetter) {
        fieldMethodMap.put(psiField, propertySetter);
      }
    }

    processIntern(fieldMethodMap, psiClass, LombokClassNames.SETTER);
  }

}
