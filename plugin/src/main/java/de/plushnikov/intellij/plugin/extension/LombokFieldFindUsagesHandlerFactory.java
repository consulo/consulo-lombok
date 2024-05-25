package de.plushnikov.intellij.plugin.extension;

import com.intellij.java.language.psi.PsiClass;
import com.intellij.java.language.psi.PsiField;
import com.intellij.java.language.psi.PsiMember;
import com.intellij.java.language.psi.PsiRecordComponent;
import consulo.annotation.component.ExtensionImpl;
import consulo.find.FindUsagesHandler;
import consulo.find.FindUsagesHandlerFactory;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiUtilCore;
import consulo.project.DumbService;
import de.plushnikov.intellij.plugin.psi.LombokLightClassBuilder;
import de.plushnikov.intellij.plugin.psi.LombokLightFieldBuilder;
import de.plushnikov.intellij.plugin.psi.LombokLightMethodBuilder;
import jakarta.annotation.Nonnull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 * It should find calls to getters/setters of some field changed by lombok accessors
 */
@ExtensionImpl
public class LombokFieldFindUsagesHandlerFactory extends FindUsagesHandlerFactory {

  public LombokFieldFindUsagesHandlerFactory() {
  }

  @Override
  public boolean canFindUsages(@Nonnull PsiElement element) {
    if ((element instanceof PsiField || element instanceof PsiRecordComponent) && !DumbService.isDumb(element.getProject())) {
      final PsiMember psiMember = (PsiMember)element;
      final PsiClass containingClass = psiMember.getContainingClass();
      if (containingClass != null) {
        return Arrays.stream(containingClass.getMethods()).anyMatch(LombokLightMethodBuilder.class::isInstance) ||
          Arrays.stream(containingClass.getInnerClasses()).anyMatch(LombokLightClassBuilder.class::isInstance);
      }
    }
    return false;
  }

  @Override
  public FindUsagesHandler createFindUsagesHandler(@Nonnull PsiElement element, boolean forHighlightUsages) {
    return new FindUsagesHandler(element) {
      @Override
      @Nonnull
      public PsiElement[] getSecondaryElements() {
        final PsiMember psiMember = (PsiMember)getPsiElement();
        final PsiClass containingClass = psiMember.getContainingClass();
        if (containingClass != null) {

          final Collection<PsiElement> elements = new ArrayList<>();
          processClass(containingClass, psiMember, elements);

          Arrays.stream(containingClass.getInnerClasses())
                .forEach(psiClass -> processClass(psiClass, psiMember, elements));

          return PsiUtilCore.toPsiElementArray(elements);
        }
        return PsiElement.EMPTY_ARRAY;
      }

      private static void processClass(PsiClass containingClass, PsiMember refPsiField, Collection<PsiElement> collector) {
        processClassMethods(containingClass, refPsiField, collector);
        processClassFields(containingClass, refPsiField, collector);
      }

      private static void processClassFields(PsiClass containingClass, PsiMember refPsiField, Collection<PsiElement> collector) {
        Arrays.stream(containingClass.getFields())
              .filter(LombokLightFieldBuilder.class::isInstance)
              .filter(psiField -> psiField.getNavigationElement() == refPsiField)
              .forEach(collector::add);
      }

      private static void processClassMethods(PsiClass containingClass, PsiMember refPsiField, Collection<PsiElement> collector) {
        Arrays.stream(containingClass.getMethods())
              .filter(LombokLightMethodBuilder.class::isInstance)
              .filter(psiMethod -> psiMethod.getNavigationElement() == refPsiField)
              .forEach(collector::add);
      }
    };
  }
}
