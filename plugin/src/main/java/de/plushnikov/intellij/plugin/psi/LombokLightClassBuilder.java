package de.plushnikov.intellij.plugin.psi;

import com.intellij.java.language.impl.psi.impl.light.LightPsiClassBuilder;
import com.intellij.java.language.impl.psi.impl.source.PsiExtensibleClass;
import com.intellij.java.language.psi.*;
import consulo.document.util.TextRange;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.SyntheticElement;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.jetbrains.annotations.NonNls;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

public class LombokLightClassBuilder extends LightPsiClassBuilder implements PsiExtensibleClass, SyntheticElement {

  private final String myQualifiedName;
  private final LombokLightModifierList myModifierList;

  private boolean myIsEnum;
  private PsiField[] myFields;
  private PsiMethod[] myMethods;

  private Function<PsiClass, ? extends Collection<PsiField>> fieldSupplier = c -> Collections.emptyList();
  private Function<PsiClass, ? extends Collection<PsiMethod>> methodSupplier = c -> Collections.emptyList();

  public LombokLightClassBuilder(@Nonnull PsiElement context, @Nonnull String simpleName, @Nonnull String qualifiedName) {
    super(context, simpleName);
    myIsEnum = false;
    myQualifiedName = qualifiedName;
    myModifierList = new LombokLightModifierList(context.getManager(), context.getLanguage());
  }

  @Nonnull
  @Override
  public LombokLightModifierList getModifierList() {
    return myModifierList;
  }

  @Override
  public PsiElement getScope() {
    if (getContainingClass() != null) {
      return getContainingClass().getScope();
    }
    return super.getScope();
  }

  @Override
  public PsiElement getParent() {
    return getContainingClass();
  }

  @Nullable
  @Override
  public String getQualifiedName() {
    return myQualifiedName;
  }

  @Override
  public TextRange getTextRange() {
    TextRange r = super.getTextRange();
    return r == null ? TextRange.EMPTY_RANGE : r;
  }

  @Override
  public PsiFile getContainingFile() {
    if (null != getContainingClass()) {
      return getContainingClass().getContainingFile();
    }
    return super.getContainingFile();
  }

  @Override
  public boolean isEnum() {
    return myIsEnum;
  }

  @Override
  @Nonnull
  public PsiField[] getFields() {
    if (null == myFields) {
      Collection<PsiField> generatedFields = fieldSupplier.apply(this);
      myFields = generatedFields.toArray(PsiField.EMPTY_ARRAY);
      fieldSupplier = c -> Collections.emptyList();
    }
    return myFields;
  }

  @Override
  @Nonnull
  public PsiMethod[] getMethods() {
    if (null == myMethods) {
      Collection<PsiMethod> generatedMethods = methodSupplier.apply(this);
      myMethods = generatedMethods.toArray(PsiMethod.EMPTY_ARRAY);
      methodSupplier = c -> Collections.emptyList();
    }
    return myMethods;
  }

  @Override
  public @Nonnull List<PsiField> getOwnFields() {
    return Collections.emptyList();
  }

  @Override
  public @Nonnull List<PsiMethod> getOwnMethods() {
    return Collections.emptyList();
  }

  @Override
  public @Nonnull List<PsiClass> getOwnInnerClasses() {
    return Collections.emptyList();
  }

  public LombokLightClassBuilder withFieldSupplier(final Function<PsiClass, ? extends Collection<PsiField>> fieldSupplier) {
    this.fieldSupplier = fieldSupplier;
    return this;
  }

  public LombokLightClassBuilder withMethodSupplier(final Function<PsiClass, ? extends Collection<PsiMethod>> methodSupplier) {
    this.methodSupplier = methodSupplier;
    return this;
  }

  public LombokLightClassBuilder withEnum(boolean isEnum) {
    myIsEnum = isEnum;
    return this;
  }

  public LombokLightClassBuilder withImplicitModifier(@PsiModifier.ModifierConstant @Nonnull @NonNls String modifier) {
    myModifierList.addImplicitModifierProperty(modifier);
    return this;
  }

  public LombokLightClassBuilder withModifier(@PsiModifier.ModifierConstant @Nonnull @NonNls String modifier) {
    myModifierList.addModifier(modifier);
    return this;
  }

  public LombokLightClassBuilder withContainingClass(@Nonnull PsiClass containingClass) {
    setContainingClass(containingClass);
    return this;
  }

  public LombokLightClassBuilder withNavigationElement(PsiElement navigationElement) {
    setNavigationElement(navigationElement);
    return this;
  }

  public LombokLightClassBuilder withExtends(PsiClassType baseClassType) {
    getExtendsList().addReference(baseClassType);
    return this;
  }

  public LombokLightClassBuilder withParameterTypes(@Nullable PsiTypeParameterList parameterList) {
    if (parameterList != null) {
      Stream.of(parameterList.getTypeParameters()).forEach(this::withParameterType);
    }
    return this;
  }

  public LombokLightClassBuilder withParameterType(@Nonnull PsiTypeParameter psiTypeParameter) {
    getTypeParameterList().addParameter(psiTypeParameter);
    return this;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    LombokLightClassBuilder that = (LombokLightClassBuilder)o;

    return myQualifiedName.equals(that.myQualifiedName);
  }

  @Override
  public int hashCode() {
    return myQualifiedName.hashCode();
  }
}
