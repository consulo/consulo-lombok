package de.plushnikov.intellij.plugin.util;

import com.intellij.java.language.psi.PsiMethod;
import jakarta.annotation.Nonnull;

import java.util.Collection;

/**
 * @author Plushnikov Michail
 */
public final class PsiMethodUtil {

  public static boolean hasMethodByName(@Nonnull Collection<PsiMethod> classMethods, @Nonnull String methodName, int paramCount) {
    return classMethods.stream()
      .filter(m -> methodName.equals(m.getName()))
      .anyMatch(m -> acceptedParameterCount(m, paramCount));
  }

  public static boolean hasSimilarMethod(@Nonnull Collection<PsiMethod> classMethods, @Nonnull String methodName, int paramCount) {
    return classMethods.stream()
      .filter(m -> methodName.equalsIgnoreCase(m.getName()))
      .anyMatch(m -> acceptedParameterCount(m, paramCount));
  }

  private static boolean acceptedParameterCount(@Nonnull PsiMethod classMethod, int methodArgCount) {
    int minArgs = classMethod.getParameterList().getParametersCount();
    int maxArgs = minArgs;
    if (classMethod.isVarArgs()) {
      minArgs--;
      maxArgs = Integer.MAX_VALUE;
    }
    return !(methodArgCount < minArgs || methodArgCount > maxArgs);
  }
}
