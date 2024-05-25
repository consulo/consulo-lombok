package de.plushnikov.intellij.plugin.util;

import com.intellij.java.language.psi.PsiAnnotation;
import com.intellij.java.language.psi.PsiJavaCodeReferenceElement;
import com.intellij.java.language.psi.PsiModifierListOwner;
import consulo.util.collection.ContainerUtil;
import consulo.util.lang.StringUtil;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public final class PsiAnnotationSearchUtil {

  @Nullable
  public static PsiAnnotation findAnnotation(@Nonnull PsiModifierListOwner psiModifierListOwner, @Nonnull String annotationFQN) {
    return psiModifierListOwner.getAnnotation(annotationFQN);
  }

  @Nullable
  public static PsiAnnotation findAnnotation(@Nonnull PsiModifierListOwner psiModifierListOwner, @Nonnull String... annotationFQNs) {
    for (String annotationFQN : annotationFQNs) {
      PsiAnnotation annotation = psiModifierListOwner.getAnnotation(annotationFQN);
      if (annotation != null) {
        return annotation;
      }
    }
    return null;
  }

  public static boolean isAnnotatedWith(@Nonnull PsiModifierListOwner psiModifierListOwner, @Nonnull String annotationFQN) {
    return psiModifierListOwner.hasAnnotation(annotationFQN);
  }

  public static boolean isNotAnnotatedWith(@Nonnull PsiModifierListOwner psiModifierListOwner, String annotationTypeName) {
    return !isAnnotatedWith(psiModifierListOwner, annotationTypeName);
  }

  public static boolean isAnnotatedWith(@Nonnull PsiModifierListOwner psiModifierListOwner, @Nonnull String... annotationTypes) {
    return null != findAnnotation(psiModifierListOwner, annotationTypes);
  }

  public static boolean isNotAnnotatedWith(@Nonnull PsiModifierListOwner psiModifierListOwner, @Nonnull String... annotationTypes) {
    return !isAnnotatedWith(psiModifierListOwner, annotationTypes);
  }

  public static List<PsiAnnotation> findAllAnnotations(@Nonnull PsiModifierListOwner listOwner,
                                                       @Nonnull Collection<String> annotationNames) {
    if (annotationNames.isEmpty()) {
      return Collections.emptyList();
    }

    List<PsiAnnotation> result = new ArrayList<>();
    for (PsiAnnotation annotation : listOwner.getAnnotations()) {
      if (ContainerUtil.exists(annotationNames, annotation::hasQualifiedName)) {
        result.add(annotation);
      }
    }
    return result;
  }

  @Nonnull
  public static String getShortNameOf(@Nonnull PsiAnnotation psiAnnotation) {
    PsiJavaCodeReferenceElement referenceElement = psiAnnotation.getNameReferenceElement();
    return StringUtil.notNullize(null == referenceElement ? null : referenceElement.getReferenceName());
  }

  public static boolean checkAnnotationsSimpleNameExistsIn(@Nonnull PsiModifierListOwner modifierListOwner,
                                                           @Nonnull Collection<String> annotationNames) {
    for (PsiAnnotation psiAnnotation : modifierListOwner.getAnnotations()) {
      final String shortName = getShortNameOf(psiAnnotation);
      if (annotationNames.contains(shortName)) {
        return true;
      }
    }
    return false;
  }

  @Nullable
  public static PsiAnnotation findAnnotationByShortNameOnly(@Nonnull PsiModifierListOwner psiModifierListOwner,
                                                            @Nonnull String... annotationFQNs) {
    if (annotationFQNs.length > 0) {
      Collection<String> possibleShortNames = ContainerUtil.map(annotationFQNs, StringUtil::getShortName);

      for (PsiAnnotation psiAnnotation : psiModifierListOwner.getAnnotations()) {
        String shortNameOfAnnotation = getShortNameOf(psiAnnotation);
        if (possibleShortNames.contains(shortNameOfAnnotation)) {
          return psiAnnotation;
        }
      }
    }
    return null;
  }

  public static boolean checkAnnotationHasOneOfFQNs(@Nonnull PsiAnnotation psiAnnotation,
                                                    @Nonnull String... annotationFQNs) {
    return ContainerUtil.or(annotationFQNs, psiAnnotation::hasQualifiedName);
  }
}
