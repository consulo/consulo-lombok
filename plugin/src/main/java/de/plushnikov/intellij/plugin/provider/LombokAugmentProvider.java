package de.plushnikov.intellij.plugin.provider;

import com.intellij.java.language.JavaLanguage;
import com.intellij.java.language.impl.psi.impl.source.PsiExtensibleClass;
import com.intellij.java.language.psi.*;
import com.intellij.java.language.psi.augment.PsiAugmentProvider;
import com.intellij.java.language.psi.augment.PsiExtensionMethod;
import consulo.annotation.component.ExtensionImpl;
import consulo.application.Application;
import consulo.language.psi.PsiCompiledElement;
import consulo.language.psi.PsiElement;
import de.plushnikov.intellij.plugin.LombokClassNames;
import de.plushnikov.intellij.plugin.processor.LombokProcessorManager;
import de.plushnikov.intellij.plugin.processor.Processor;
import de.plushnikov.intellij.plugin.processor.ValProcessor;
import de.plushnikov.intellij.plugin.processor.method.ExtensionMethodsHelper;
import de.plushnikov.intellij.plugin.processor.modifier.ModifierProcessor;
import de.plushnikov.intellij.plugin.util.PsiAnnotationSearchUtil;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.inject.Inject;

import java.util.*;

import static de.plushnikov.intellij.plugin.util.LombokLibraryUtil.hasLombokLibrary;

/**
 * Provides support for lombok generated elements
 *
 * @author Plushnikov Michail
 */
@ExtensionImpl
public class LombokAugmentProvider extends PsiAugmentProvider {
  private final Application myApplication;

  @Inject
  public LombokAugmentProvider(Application application) {
    myApplication = application;
  }

  @Nonnull
  @Override
  protected Set<String> transformModifiers(@Nonnull PsiModifierList modifierList, @Nonnull final Set<String> modifiers) {
    // skip if no lombok library is present
    if (!hasLombokLibrary(modifierList.getProject())) {
      return modifiers;
    }

    // make copy of original modifiers
    Set<String> result = new HashSet<>(modifiers);

    // Loop through all available processors and give all of them a chance to respond
    myApplication.getExtensionPoint(ModifierProcessor.class).forEachExtensionSafe(modifierProcessor -> {
      if (modifierProcessor.isSupported(modifierList)) {
        modifierProcessor.transformModifiers(modifierList, result);
      }
    });

    return result;
  }

  @Override
  public boolean canInferType(@Nonnull PsiTypeElement typeElement) {
    return hasLombokLibrary(typeElement.getProject()) && ValProcessor.canInferType(typeElement);
  }

  /*
   * The final fields that are marked with Builder.Default contains only possible value
   * because user can set another value during the creation of the object.
   */
  //see de.plushnikov.intellij.plugin.inspection.DataFlowInspectionTest.testDefaultBuilderFinalValueInspectionIsAlwaysThat
  //see de.plushnikov.intellij.plugin.inspection.PointlessBooleanExpressionInspectionTest.testPointlessBooleanExpressionBuilderDefault
  @Override
  protected boolean fieldInitializerMightBeChanged(@Nonnull PsiField field) {
    return PsiAnnotationSearchUtil.isAnnotatedWith(field, LombokClassNames.BUILDER_DEFAULT);
  }

  @Nullable
  @Override
  protected PsiType inferType(@Nonnull PsiTypeElement typeElement) {
    return hasLombokLibrary(typeElement.getProject()) ? ValProcessor.inferType(typeElement) : null;
  }

  @Nonnull
  @Override
  public <Psi extends PsiElement> List<Psi> getAugments(@Nonnull PsiElement element,
                                                        @Nonnull final Class<Psi> type) {
    return getAugments(element, type, null);
  }

  @Nonnull
  @Override
  public <Psi extends PsiElement> List<Psi> getAugments(@Nonnull PsiElement element,
                                                        @Nonnull final Class<Psi> type,
                                                        @Nullable String nameHint) {
    final List<Psi> emptyResult = Collections.emptyList();
    if ((type != PsiClass.class && type != PsiField.class && type != PsiMethod.class) || !(element instanceof PsiExtensibleClass)
        || (element instanceof PsiCompiledElement) // skip compiled classes
        ) {
      return emptyResult;
    }

    final PsiClass psiClass = (PsiClass) element;
    if (!psiClass.getLanguage().isKindOf(JavaLanguage.INSTANCE)) {
      return emptyResult;
    }
    // Skip processing of Annotations and Interfaces
    if (psiClass.isAnnotationType() || psiClass.isInterface()) {
      return emptyResult;
    }
    // skip processing if disabled, or no lombok library is present
    if (!hasLombokLibrary(element.getProject())) {
      return emptyResult;
    }

    // All invoker of AugmentProvider already make caching,
    // and we want to try to skip recursive calls completely

    return getPsis(psiClass, type, nameHint);
  }

  @Nonnull
  private static <Psi extends PsiElement> List<Psi> getPsis(PsiClass psiClass, Class<Psi> type, String nameHint) {
    final List<Psi> result = new ArrayList<>();
    for (Processor processor : LombokProcessorManager.getProcessors(type)) {
      final List<? super PsiElement> generatedElements = processor.process(psiClass, nameHint);
      for (Object psiElement : generatedElements) {
        result.add((Psi) psiElement);
      }
    }
    return result;
  }

  @Override
  protected List<PsiExtensionMethod> getExtensionMethods(@Nonnull PsiClass aClass,
                                                         @Nonnull String nameHint,
                                                         @Nonnull PsiElement context) {
    if (!hasLombokLibrary(context.getProject())) {
      return Collections.emptyList();
    }
    return ExtensionMethodsHelper.getExtensionMethods(aClass, nameHint, context);
  }
}
