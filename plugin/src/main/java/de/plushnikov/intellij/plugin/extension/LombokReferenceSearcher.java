package de.plushnikov.intellij.plugin.extension;

import com.intellij.java.language.psi.PsiClass;
import com.intellij.java.language.psi.PsiField;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiReference;
import consulo.language.psi.search.ReferencesSearch;
import consulo.language.psi.search.ReferencesSearchQueryExecutor;
import consulo.language.psi.search.SearchRequestCollector;
import consulo.language.psi.search.UsageSearchContext;
import consulo.project.DumbService;
import consulo.project.util.query.QueryExecutorBase;
import de.plushnikov.intellij.plugin.psi.LombokLightFieldBuilder;
import de.plushnikov.intellij.plugin.psi.LombokLightMethodBuilder;
import jakarta.annotation.Nonnull;

import java.util.Arrays;
import java.util.function.Predicate;

/**
 * Annas example: org.jetbrains.plugins.javaFX.fxml.refs.JavaFxControllerFieldSearcher
 * Alternative Implementation for LombokFieldFindUsagesHandlerFactory
 */
public class LombokReferenceSearcher extends QueryExecutorBase<PsiReference, ReferencesSearch.SearchParameters> implements ReferencesSearchQueryExecutor {

    public LombokReferenceSearcher() {
        super(true);
    }

    @Override
    public void processQuery(@Nonnull ReferencesSearch.SearchParameters queryParameters, @Nonnull Predicate consumer) {
        PsiElement refElement = queryParameters.getElementToSearch();

        if (refElement instanceof PsiField) {
            DumbService.getInstance(queryParameters.getProject()).runReadActionInSmartMode(() ->
                processPsiField((PsiField) refElement,
                    queryParameters.getOptimizer()));
        }
    }

    private static void processPsiField(final PsiField refPsiField, final SearchRequestCollector collector) {
        final PsiClass containingClass = refPsiField.getContainingClass();
        if (null != containingClass) {
            processClassMethods(refPsiField, collector, containingClass);

            final PsiClass[] innerClasses = containingClass.getInnerClasses();
            Arrays.stream(innerClasses)
                .forEach(psiClass -> processClassMethods(refPsiField, collector, psiClass));

            Arrays.stream(innerClasses)
                .forEach(psiClass -> processClassFields(refPsiField, collector, psiClass));
        }
    }

    private static void processClassMethods(PsiField refPsiField, SearchRequestCollector collector, PsiClass containingClass) {
        Arrays.stream(containingClass.getMethods())
            .filter(LombokLightMethodBuilder.class::isInstance)
            .filter(psiMethod -> psiMethod.getNavigationElement() == refPsiField)
            .forEach(psiMethod -> {
                collector.searchWord(psiMethod.getName(), psiMethod.getUseScope(), UsageSearchContext.IN_CODE, true, psiMethod);
            });
    }

    private static void processClassFields(PsiField refPsiField, SearchRequestCollector collector, PsiClass containingClass) {
        Arrays.stream(containingClass.getFields())
            .filter(LombokLightFieldBuilder.class::isInstance)
            .filter(psiField -> psiField.getNavigationElement() == refPsiField)
            .forEach(psiField -> {
                collector.searchWord(psiField.getName(), psiField.getUseScope(), UsageSearchContext.IN_CODE, true, psiField);
            });
    }

}
