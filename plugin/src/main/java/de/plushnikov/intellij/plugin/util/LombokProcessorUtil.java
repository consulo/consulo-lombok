package de.plushnikov.intellij.plugin.util;

import com.intellij.java.language.psi.*;
import com.intellij.java.language.psi.util.PsiUtil;
import consulo.util.collection.ContainerUtil;
import consulo.util.lang.StringUtil;
import de.plushnikov.intellij.plugin.LombokClassNames;
import jakarta.annotation.Nonnull;
import org.jetbrains.annotations.NonNls;
import jakarta.annotation.Nullable;

import java.util.*;

/**
 * @author Plushnikov Michail
 */
public final class LombokProcessorUtil {

  @NonNls
  private static final String ACCESS_LEVEL_PRIVATE = "PRIVATE";
  @NonNls
  private static final String ACCESS_LEVEL_PROTECTED = "PROTECTED";
  @NonNls
  private static final String ACCESS_LEVEL_PACKAGE_LOCAL = "PACKAGE";
  @NonNls
  private static final String ACCESS_LEVEL_PUBLIC = "PUBLIC";
  @NonNls
  private static final String ACCESS_LEVEL_NONE = "NONE";
  @NonNls
  private static final String ACCESS_LEVEL_MODULE = "MODULE";

  private static final Map<Integer, String> ACCESS_LEVEL_MAP = Map.of(
    PsiUtil.ACCESS_LEVEL_PUBLIC, ACCESS_LEVEL_PUBLIC,
    PsiUtil.ACCESS_LEVEL_PACKAGE_LOCAL, ACCESS_LEVEL_PACKAGE_LOCAL,
    PsiUtil.ACCESS_LEVEL_PROTECTED, ACCESS_LEVEL_PROTECTED,
    PsiUtil.ACCESS_LEVEL_PRIVATE, ACCESS_LEVEL_PRIVATE);

  private static final Map<String, String> VALUE_ACCESS_LEVEL_MAP = Map.of(
    ACCESS_LEVEL_PUBLIC, PsiModifier.PUBLIC,
    ACCESS_LEVEL_PACKAGE_LOCAL, PsiModifier.PACKAGE_LOCAL,
    ACCESS_LEVEL_PROTECTED, PsiModifier.PROTECTED,
    ACCESS_LEVEL_PRIVATE, PsiModifier.PRIVATE);

  private static final String NULL_DEFAULT = "@@@NULL@@@";

  @Nullable
  @PsiModifier.ModifierConstant
  public static String getMethodModifier(@Nonnull PsiAnnotation psiAnnotation) {
    return getLevelVisibility(psiAnnotation, "value");
  }

  @Nullable
  @PsiModifier.ModifierConstant
  public static String getAccessVisibility(@Nonnull PsiAnnotation psiAnnotation) {
    return getLevelVisibility(psiAnnotation, "access");
  }

  @Nullable
  @PsiModifier.ModifierConstant
  public static String getLevelVisibility(@Nonnull PsiAnnotation psiAnnotation) {
    return getLevelVisibility(psiAnnotation, "level");
  }

  @Nullable
  @PsiModifier.ModifierConstant
  private static String getLevelVisibility(@Nonnull PsiAnnotation psiAnnotation, @Nonnull String parameter) {
    return convertAccessLevelToJavaModifier(PsiAnnotationUtil.getEnumAnnotationValue(psiAnnotation, parameter, ACCESS_LEVEL_PUBLIC));
  }

  @Nullable
  public static String getAccessLevel(@Nonnull PsiAnnotation psiAnnotation, @Nonnull String parameter) {
    final String annotationValue = PsiAnnotationUtil.getEnumAnnotationValue(psiAnnotation, parameter, NULL_DEFAULT);
    return NULL_DEFAULT.equals(annotationValue) ? null : VALUE_ACCESS_LEVEL_MAP.get(annotationValue);
  }

  public static boolean isLevelVisible(@Nonnull PsiAnnotation psiAnnotation) {
    return null != getLevelVisibility(psiAnnotation);
  }

  public static Iterable<String> getOnX(@Nonnull PsiAnnotation psiAnnotation, @Nonnull String parameterName) {
    Collection<String> oldOnX = getOldOnX(psiAnnotation, parameterName);
    Collection<String> newOnX = getNewOnX(psiAnnotation, parameterName + "_");
    return ContainerUtil.concat(oldOnX, newOnX);
  }

  public static Collection<String> getOldOnX(@Nonnull PsiAnnotation psiAnnotation, @Nonnull String parameterName) {
    PsiAnnotationMemberValue onXValue = psiAnnotation.hasAttribute(parameterName) ? psiAnnotation.findAttributeValue(parameterName) : null;
    if (!(onXValue instanceof PsiAnnotation)) {
      return Collections.emptyList();
    }
    Collection<PsiAnnotation> annotations = PsiAnnotationUtil.getAnnotationValues((PsiAnnotation)onXValue, "value", PsiAnnotation.class);
    return collectAnnotationStrings(annotations);
  }

  public static Collection<String> getNewOnX(@Nonnull PsiAnnotation psiAnnotation, @Nonnull String parameterName) {
    if (psiAnnotation.hasAttribute(parameterName)) {
      final Collection<PsiAnnotation> annotations =
        PsiAnnotationUtil.getAnnotationValues(psiAnnotation, parameterName, PsiAnnotation.class);
      return collectAnnotationStrings(annotations);
    }
    return Collections.emptyList();
  }

  private static Collection<String> collectAnnotationStrings(Collection<PsiAnnotation> annotations) {
    Collection<String> annotationStrings = new ArrayList<>();
    for (PsiAnnotation annotation : annotations) {
      final String annotationQualifiedName = annotation.getQualifiedName();
      if (!StringUtil.isEmptyOrSpaces(annotationQualifiedName)) {
        PsiAnnotationParameterList params = annotation.getParameterList();
        annotationStrings.add(annotationQualifiedName + params.getText());
      }
    }
    return annotationStrings;
  }

  @Nullable
  @PsiModifier.ModifierConstant
  private static String convertAccessLevelToJavaModifier(String value) {
    if (null == value || value.isEmpty()) {
      return PsiModifier.PUBLIC;
    }
    if (ACCESS_LEVEL_MODULE.equals(value)) {
      return PsiModifier.PACKAGE_LOCAL;
    }
    if (ACCESS_LEVEL_NONE.equals(value)) {
      return null;
    }
    return VALUE_ACCESS_LEVEL_MAP.get(value);
  }

  @Nonnull
  public static PsiAnnotation createAnnotationWithAccessLevel(@Nonnull PsiModifierListOwner psiModifierListOwner,
                                                              String annotationClassName) {
    Optional<String> value = convertModifierToLombokAccessLevel(psiModifierListOwner);
    return PsiAnnotationUtil.createPsiAnnotation(psiModifierListOwner, value.orElse(""), annotationClassName);
  }

  @Nonnull
  public static Optional<String> convertModifierToLombokAccessLevel(@Nonnull PsiModifierListOwner psiModifierListOwner) {
    final PsiModifierList modifierList = psiModifierListOwner.getModifierList();
    if (null != modifierList) {
      final int accessLevelCode = PsiUtil.getAccessLevel(modifierList);

      final String accessLevel = ACCESS_LEVEL_MAP.get(accessLevelCode);
      if (null != accessLevel && !ACCESS_LEVEL_PUBLIC.equals(accessLevel)) {
        return Optional.of(LombokClassNames.ACCESS_LEVEL + "." + accessLevel);
      }
    }
    return Optional.empty();
  }
}
