package de.plushnikov.intellij.plugin.processor.clazz;

import com.intellij.java.language.psi.*;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import de.plushnikov.intellij.plugin.LombokClassNames;
import de.plushnikov.intellij.plugin.problem.ProblemSink;
import de.plushnikov.intellij.plugin.psi.LombokLightMethodBuilder;
import de.plushnikov.intellij.plugin.util.PsiClassUtil;
import jakarta.annotation.Nonnull;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author Florian Böhm
 */
@ExtensionImpl
public class UtilityClassProcessor extends AbstractClassProcessor {

  public UtilityClassProcessor() {
    super(PsiMethod.class, LombokClassNames.UTILITY_CLASS);
  }

  @Override
  protected Collection<String> getNamesOfPossibleGeneratedElements(@Nonnull PsiClass psiClass, @Nonnull PsiAnnotation psiAnnotation) {
    return Collections.singleton(psiClass.getName());
  }

  @Override
  protected boolean validate(@Nonnull PsiAnnotation psiAnnotation, @Nonnull PsiClass psiClass, @Nonnull ProblemSink builder) {
    return validateOnRightType(psiClass, builder) && validateNoConstructorsDefined(psiClass, builder);
  }

  private static boolean validateNoConstructorsDefined(PsiClass psiClass, ProblemSink builder) {
    Collection<PsiMethod> psiMethods = PsiClassUtil.collectClassConstructorIntern(psiClass);
    if (!psiMethods.isEmpty()) {
      builder.addErrorMessage("inspection.message.utility.classes.cannot.have.declared.constructors");
      return false;
    }
    return true;
  }

  public static boolean validateOnRightType(PsiClass psiClass, ProblemSink builder) {
    if (checkWrongType(psiClass)) {
      builder.addErrorMessage("inspection.message.utility.class.only.supported.on.class");
      return false;
    }
    PsiElement context = psiClass.getContext();
    if (context == null) {
      return false;
    }
    if (!(context instanceof PsiFile)) {
      PsiElement contextUp = context;
      while (true) {
        if (contextUp instanceof PsiClass psiClassUp) {
          if (psiClassUp.getContext() instanceof PsiFile) {
            return true;
          }
          boolean isStatic = isStatic(psiClassUp.getModifierList());
          if (isStatic || checkWrongType(psiClassUp)) {
            contextUp = contextUp.getContext();
          } else {
            builder.addErrorMessage("inspection.message.utility.class.automatically.makes.class.static");
            return false;
          }
        } else {
          builder.addErrorMessage("inspection.message.utility.class.cannot.be.placed");
          return false;
        }
      }
    }
    return true;
  }

  private static boolean isStatic(PsiModifierList modifierList) {
    return modifierList != null && modifierList.hasModifierProperty(PsiModifier.STATIC);
  }

  private static boolean checkWrongType(PsiClass psiClass) {
    return psiClass != null && (psiClass.isInterface() || psiClass.isEnum() || psiClass.isAnnotationType());
  }

  @Override
  protected void generatePsiElements(@Nonnull final PsiClass psiClass, @Nonnull PsiAnnotation psiAnnotation, @Nonnull List<? super PsiElement> target) {

    LombokLightMethodBuilder constructorBuilder = new LombokLightMethodBuilder(psiClass.getManager(), psiClass.getName())
      .withConstructor(true)
      .withContainingClass(psiClass)
      .withNavigationElement(psiAnnotation)
      .withModifier(PsiModifier.PRIVATE)
      .withBodyText(String.format("throw new %s(%s);",
                                  "java.lang.UnsupportedOperationException", "\"This is a utility class and cannot be instantiated\""));
    target.add(constructorBuilder);
  }
}
