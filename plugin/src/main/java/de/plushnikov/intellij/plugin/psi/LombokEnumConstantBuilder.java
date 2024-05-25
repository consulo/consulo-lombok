package de.plushnikov.intellij.plugin.psi;

import com.intellij.java.language.psi.*;
import consulo.language.psi.PsiManager;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class LombokEnumConstantBuilder extends LombokLightFieldBuilder implements PsiEnumConstant {
  public LombokEnumConstantBuilder(@Nonnull PsiManager manager, @Nonnull String name, @Nonnull PsiType type) {
    super(manager, name, type);
  }

  @Nullable
  @Override
  public PsiExpressionList getArgumentList() {
    return null;
  }

  @Nullable
  @Override
  public PsiEnumConstantInitializer getInitializingClass() {
    return null;
  }

  @Nonnull
  @Override
  public PsiEnumConstantInitializer getOrCreateInitializingClass() {
    final PsiElementFactory factory = JavaPsiFacade.getElementFactory(getProject());
    return factory.createEnumConstantFromText("foo{}", null).getInitializingClass();
  }

  @Nullable
  @Override
  public PsiMethod resolveMethod() {
    return null;
  }

  @Nonnull
  @Override
  public JavaResolveResult resolveMethodGenerics() {
    return JavaResolveResult.EMPTY;
  }

  @Nullable
  @Override
  public PsiMethod resolveConstructor() {
    return null;
  }
}
