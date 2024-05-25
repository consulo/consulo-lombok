package de.plushnikov.intellij.plugin.util;

import com.intellij.java.language.impl.psi.impl.source.PsiExtensibleClass;
import com.intellij.java.language.psi.*;
import consulo.language.psi.PsiElement;
import consulo.util.collection.ContainerUtil;
import jakarta.annotation.Nonnull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Plushnikov Michail
 */
public final class PsiClassUtil {

  /**
   * Workaround to get all of original Methods of the psiClass, without calling PsiAugmentProvider infinitely
   *
   * @param psiClass psiClass to collect all of methods from
   * @return all intern methods of the class
   */
  @Nonnull
  public static Collection<PsiMethod> collectClassMethodsIntern(@Nonnull PsiClass psiClass) {
    if (psiClass instanceof PsiExtensibleClass) {
      return new ArrayList<>(((PsiExtensibleClass) psiClass).getOwnMethods());
    } else {
      return filterPsiElements(psiClass, PsiMethod.class);
    }
  }

  /**
   * Workaround to get all of original Fields of the psiClass, without calling PsiAugmentProvider infinitely
   *
   * @param psiClass psiClass to collect all of fields from
   * @return all intern fields of the class
   */
  @Nonnull
  public static Collection<PsiField> collectClassFieldsIntern(@Nonnull PsiClass psiClass) {
    if (psiClass instanceof PsiExtensibleClass) {
      return ((PsiExtensibleClass) psiClass).getOwnFields();
    } else {
      return filterPsiElements(psiClass, PsiField.class);
    }
  }

  /**
   * Workaround to get all of original inner classes of the psiClass, without calling PsiAugmentProvider infinitely
   *
   * @param psiClass psiClass to collect all inner classes from
   * @return all inner classes of the class
   */
  @Nonnull
  public static Collection<PsiClass> collectInnerClassesIntern(@Nonnull PsiClass psiClass) {
    if (psiClass instanceof PsiExtensibleClass) {
      return ((PsiExtensibleClass) psiClass).getOwnInnerClasses();
    } else {
      return filterPsiElements(psiClass, PsiClass.class);
    }
  }

  @Nonnull
  public static Collection<PsiMember> collectClassMemberIntern(@Nonnull PsiClass psiClass) {
    return Arrays.stream(psiClass.getChildren()).filter(e -> e instanceof PsiField || e instanceof PsiMethod).map(PsiMember.class::cast).collect(Collectors.toList());
  }

  private static <T extends PsiElement> Collection<T> filterPsiElements(@Nonnull PsiClass psiClass, @Nonnull Class<T> desiredClass) {
    return Arrays.stream(psiClass.getChildren()).filter(desiredClass::isInstance).map(desiredClass::cast).collect(Collectors.toList());
  }

  @Nonnull
  public static Collection<PsiMethod> collectClassConstructorIntern(@Nonnull PsiClass psiClass) {
    final Collection<PsiMethod> psiMethods = collectClassMethodsIntern(psiClass);
    return ContainerUtil.filter(psiMethods, PsiMethod::isConstructor);
  }

  @Nonnull
  public static Collection<PsiMethod> collectClassStaticMethodsIntern(@Nonnull PsiClass psiClass) {
    final Collection<PsiMethod> psiMethods = collectClassMethodsIntern(psiClass);
    return ContainerUtil.filter(psiMethods, psiMethod -> psiMethod.hasModifierProperty(PsiModifier.STATIC));
  }

  public static boolean hasSuperClass(@Nonnull final PsiClass psiClass) {
    final PsiClass superClass = psiClass.getSuperClass();
    return (null != superClass && !CommonClassNames.JAVA_LANG_OBJECT.equals(superClass.getQualifiedName())
      || !superTypesIsEmptyOrObjectOnly(psiClass));
  }

  private static boolean superTypesIsEmptyOrObjectOnly(@Nonnull final PsiClass psiClass) {
    // It returns abstract classes, but also Object.
    final PsiClassType[] superTypes = psiClass.getSuperTypes();
    return superTypes.length != 1 || CommonClassNames.JAVA_LANG_OBJECT.equals(superTypes[0].getCanonicalText());
  }

  @Nonnull
  public static PsiClassType getWildcardClassType(@Nonnull PsiClass psiClass) {
    final PsiElementFactory elementFactory = JavaPsiFacade.getElementFactory(psiClass.getProject());
    if (psiClass.hasTypeParameters()) {
      PsiType[] wildcardTypes = new PsiType[psiClass.getTypeParameters().length];
      Arrays.fill(wildcardTypes, PsiWildcardType.createUnbounded(psiClass.getManager()));
      return elementFactory.createType(psiClass, wildcardTypes);
    }
    return elementFactory.createType(psiClass);
  }

  /**
   * Creates a PsiType for a PsiClass enriched with generic substitution information if available
   */
  @Nonnull
  public static PsiClassType getTypeWithGenerics(@Nonnull PsiClass psiClass) {
    final PsiElementFactory factory = JavaPsiFacade.getElementFactory(psiClass.getProject());
    if (psiClass.hasTypeParameters()) {
      final PsiType[] psiTypes = Stream.of(psiClass.getTypeParameters()).map(factory::createType).toArray(PsiType[]::new);
      return factory.createType(psiClass, psiTypes);
    }
    else {
      return factory.createType(psiClass);
    }
  }

  /**
   * Workaround to get inner class of the psiClass, without calling PsiAugmentProvider infinitely
   *
   * @param psiClass psiClass to search for inner class
   * @return inner class if found
   */
  public static Optional<PsiClass> getInnerClassInternByName(@Nonnull PsiClass psiClass, @Nonnull String className) {
    Collection<PsiClass> innerClasses = collectInnerClassesIntern(psiClass);
    return innerClasses.stream().filter(innerClass -> className.equals(innerClass.getName())).findAny();
  }

  public static Collection<String> getNames(Collection<? extends PsiMember> psiMembers) {
    return psiMembers.stream().map(PsiMember::getName).collect(Collectors.toSet());
  }
}
