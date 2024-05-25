package de.plushnikov.intellij.plugin.util;

import com.intellij.java.language.psi.*;
import com.intellij.java.language.psi.util.PsiUtil;
import consulo.language.psi.PsiManager;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.project.Project;
import consulo.util.lang.Comparing;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public final class PsiTypeUtil {

  @Nonnull
  public static PsiType extractOneElementType(@Nonnull PsiType psiType, @Nonnull PsiManager psiManager) {
    return extractOneElementType(psiType, psiManager, CommonClassNames.JAVA_LANG_ITERABLE, 0);
  }

  @Nonnull
  public static PsiType extractOneElementType(@Nonnull PsiType psiType,
                                              @Nonnull PsiManager psiManager,
                                              final String superClass,
                                              final int paramIndex) {
    PsiType oneElementType = substituteTypeParameter(psiType, superClass, paramIndex);
    if (null == oneElementType) {
      oneElementType = getJavaLangObject(psiManager);
    }
    return oneElementType;
  }

  private static PsiType substituteTypeParameter(@Nonnull PsiType psiType, String superClass, int paramIndex) {
    PsiType oneElementType = PsiUtil.substituteTypeParameter(psiType, superClass, paramIndex, true);
    if (oneElementType instanceof PsiWildcardType) {
      oneElementType = ((PsiWildcardType)oneElementType).getBound();
    }
    return oneElementType;
  }

  @Nonnull
  public static PsiType extractAllElementType(@Nonnull PsiType psiType, @Nonnull PsiManager psiManager) {
    return extractAllElementType(psiType, psiManager, CommonClassNames.JAVA_LANG_ITERABLE, 0);
  }

  @Nonnull
  public static PsiType extractAllElementType(@Nonnull PsiType psiType,
                                              @Nonnull PsiManager psiManager,
                                              final String superClass,
                                              final int paramIndex) {
    PsiType oneElementType = substituteTypeParameter(psiType, superClass, paramIndex);

    if (null == oneElementType || Comparing.equal(getJavaLangObject(psiManager), oneElementType)) {
      return PsiWildcardType.createUnbounded(psiManager);
    }
    else {
      return PsiWildcardType.createExtends(psiManager, oneElementType);
    }
  }

  @Nonnull
  private static PsiClassType getJavaLangObject(@Nonnull PsiManager psiManager) {
    return PsiType.getJavaLangObject(psiManager, GlobalSearchScope.allScope(psiManager.getProject()));
  }

  @Nonnull
  public static PsiType createCollectionType(@Nonnull PsiManager psiManager, final String collectionQualifiedName,
                                             @Nonnull PsiType... psiTypes) {
    final Project project = psiManager.getProject();
    final GlobalSearchScope globalsearchscope = GlobalSearchScope.allScope(project);
    final JavaPsiFacade facade = JavaPsiFacade.getInstance(project);

    final PsiClass genericClass = facade.findClass(collectionQualifiedName, globalsearchscope);
    if (null != genericClass) {
      return JavaPsiFacade.getElementFactory(project).createType(genericClass, psiTypes);
    }
    else {
      return getJavaLangObject(psiManager);
    }
  }

  @Nullable
  public static String getQualifiedName(@Nonnull PsiType psiType) {
    final PsiClass psiFieldClass = PsiUtil.resolveClassInType(psiType);
    return psiFieldClass != null ? psiFieldClass.getQualifiedName() : null;
  }
}
