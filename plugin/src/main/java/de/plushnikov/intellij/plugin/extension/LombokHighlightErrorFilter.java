package de.plushnikov.intellij.plugin.extension;

import com.intellij.java.analysis.impl.codeInsight.intention.AddAnnotationFix;
import com.intellij.java.language.psi.PsiClassInitializer;
import com.intellij.java.language.psi.PsiLambdaExpression;
import com.intellij.java.language.psi.PsiMethod;
import com.intellij.java.language.psi.PsiMethodReferenceExpression;
import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.codeEditor.CodeInsightColors;
import consulo.colorScheme.TextAttributesKey;
import consulo.java.analysis.impl.localize.JavaInspectionsLocalize;
import consulo.java.analysis.localize.JavaAnalysisLocalize;
import consulo.java.language.localize.JavaCompilationErrorLocalize;
import consulo.language.editor.annotation.HighlightSeverity;
import consulo.language.editor.inspection.CommonProblemDescriptor;
import consulo.language.editor.inspection.ProblemDescriptorUtil;
import consulo.language.editor.inspection.QuickFix;
import consulo.language.editor.rawHighlight.HighlightInfo;
import consulo.language.editor.rawHighlight.HighlightInfoFilter;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.localize.LocalizeKey;
import consulo.localize.LocalizeValue;
import consulo.project.Project;
import consulo.util.lang.lazy.LazyValue;
import de.plushnikov.intellij.plugin.LombokClassNames;
import de.plushnikov.intellij.plugin.handler.BuilderHandler;
import de.plushnikov.intellij.plugin.handler.FieldNameConstantsHandler;
import de.plushnikov.intellij.plugin.handler.LazyGetterHandler;
import de.plushnikov.intellij.plugin.handler.OnXAnnotationHandler;
import de.plushnikov.intellij.plugin.util.LombokLibraryUtil;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.*;

@ExtensionImpl
public class LombokHighlightErrorFilter implements HighlightInfoFilter {
    private static final class Holder {
        static final Map<HighlightSeverity, Map<TextAttributesKey, List<LombokHighlightFilter>>> registeredFilters;
        static final Map<HighlightSeverity, Map<TextAttributesKey, List<LombokHighlightFixHook>>> registeredHooks;

        static {
            registeredFilters = new HashMap<>();
            registeredHooks = new HashMap<>();

            for (LombokHighlightFilter highlightFilter : LombokHighlightFilter.values()) {
                registeredFilters.computeIfAbsent(highlightFilter.severity, s -> new HashMap<>())
                    .computeIfAbsent(highlightFilter.key, k -> new ArrayList<>())
                    .add(highlightFilter);
            }

            for (LombokHighlightFixHook highlightFixHook : LombokHighlightFixHook.values()) {
                registeredHooks.computeIfAbsent(highlightFixHook.severity, s -> new HashMap<>())
                    .computeIfAbsent(highlightFixHook.key, k -> new ArrayList<>())
                    .add(highlightFixHook);
            }
        }
    }

    public LombokHighlightErrorFilter() {
    }

    @Override
    @RequiredReadAction
    public boolean accept(@Nonnull HighlightInfo highlightInfo, @Nullable PsiFile file) {
        if (null == file) {
            return true;
        }

        Project project = file.getProject();
        if (!LombokLibraryUtil.hasLombokLibrary(project)) {
            return true;
        }

        PsiElement highlightedElement = file.findElementAt(highlightInfo.getStartOffset());
        if (null == highlightedElement) {
            return true;
        }

        // check exceptions for highlights
        boolean acceptHighlight = Holder.registeredFilters
            .getOrDefault(highlightInfo.getSeverity(), Collections.emptyMap())
            .getOrDefault(highlightInfo.getType().getAttributesKey(), Collections.emptyList())
            .stream()
            .filter(filter -> filter.descriptionCheck(highlightInfo.getDescription(), highlightedElement))
            .allMatch(filter -> filter.accept(highlightedElement));

        // check if highlight was filtered
        if (!acceptHighlight) {
            return false;
        }

        // handle rest cases
        if (HighlightSeverity.ERROR.equals(highlightInfo.getSeverity())) {
            //Handling onX parameters
            if (OnXAnnotationHandler.isOnXParameterAnnotation(highlightInfo, file)
                || OnXAnnotationHandler.isOnXParameterValue(highlightInfo, file)) {
                return false;
            }
        }

        // register different quick fix for highlight
        Holder.registeredHooks
            .getOrDefault(highlightInfo.getSeverity(), Collections.emptyMap())
            .getOrDefault(highlightInfo.getType().getAttributesKey(), Collections.emptyList())
            .stream()
            .filter(filter -> filter.descriptionCheck(highlightInfo.getDescription()))
            .forEach(filter -> filter.processHook(highlightedElement, highlightInfo));

        return true;
    }

    private enum LombokHighlightFixHook {
        UNHANDLED_EXCEPTION(HighlightSeverity.ERROR, CodeInsightColors.ERRORS_ATTRIBUTES) {
            LazyValue<LocalizeKey> errorKey = LazyValue.notNull(
                () -> JavaCompilationErrorLocalize.exceptionUnhandled("", 0).getKey().get()
            );

            @Override
            public boolean descriptionCheck(@Nonnull LocalizeValue description) {
                return errorKey.get().equals(description.getKey().orElse(null));
            }

            @SuppressWarnings("unchecked")
            private final Class<PsiElement>[] CODE_BLOCK_PARENTS = new Class[] {
                PsiMethod.class,
                PsiLambdaExpression.class,
                PsiMethodReferenceExpression.class,
                PsiClassInitializer.class
            };

            @Override
            public void processHook(@Nonnull PsiElement highlightedElement, @Nonnull HighlightInfo highlightInfo) {
                PsiElement importantParent = PsiTreeUtil.getParentOfType(highlightedElement, CODE_BLOCK_PARENTS);

                // applicable only for methods
                if (importantParent instanceof PsiMethod method) {
                    highlightInfo.registerFix(new AddAnnotationFix(LombokClassNames.SNEAKY_THROWS, method));
                }
            }
        };

        private final HighlightSeverity severity;
        private final TextAttributesKey key;

        LombokHighlightFixHook(@Nonnull HighlightSeverity severity, @Nullable TextAttributesKey key) {
            this.severity = severity;
            this.key = key;
        }

        abstract public boolean descriptionCheck(@Nonnull LocalizeValue description);

        abstract public void processHook(@Nonnull PsiElement highlightedElement, @Nonnull HighlightInfo highlightInfo);
    }

    private enum LombokHighlightFilter {
        // ERROR HANDLERS

        //see com.intellij.java.lomboktest.LombokHighlightingTest.testGetterLazyVariableNotInitialized
        VARIABLE_MIGHT_NOT_BEEN_INITIALIZED(HighlightSeverity.ERROR, CodeInsightColors.ERRORS_ATTRIBUTES) {
            @Override
            @RequiredReadAction
            public boolean descriptionCheck(@Nonnull LocalizeValue description, PsiElement highlightedElement) {
                return JavaCompilationErrorLocalize.variableNotInitialized(highlightedElement.getText())
                    .equals(description);
            }

            @Override
            @RequiredReadAction
            public boolean accept(@Nonnull PsiElement highlightedElement) {
                return !LazyGetterHandler.isLazyGetterHandled(highlightedElement);
            }
        },

        //see com.intellij.java.lomboktest.LombokHighlightingTest.testFieldNameConstantsExample
        CONSTANT_EXPRESSION_REQUIRED(HighlightSeverity.ERROR, CodeInsightColors.ERRORS_ATTRIBUTES) {
            @Override
            @RequiredReadAction
            public boolean descriptionCheck(@Nonnull LocalizeValue description, PsiElement highlightedElement) {
                return JavaCompilationErrorLocalize.switchLabelConstantExpected().equals(description);
            }

            @Override
            @RequiredReadAction
            public boolean accept(@Nonnull PsiElement highlightedElement) {
                return !FieldNameConstantsHandler.isFiledNameConstants(highlightedElement);
            }
        },

        // WARNINGS HANDLERS
        //see com.intellij.java.lomboktest.LombokHighlightingTest.testBuilderWithDefaultRedundantInitializer
        VARIABLE_INITIALIZER_IS_REDUNDANT(HighlightSeverity.WARNING, CodeInsightColors.NOT_USED_ELEMENT_ATTRIBUTES) {
            LazyValue<LocalizeKey> errorKey = LazyValue.notNull(
                () -> JavaInspectionsLocalize.inspectionUnusedAssignmentProblemDescriptor2("", "").getKey().get()
            );

            @Override
            @RequiredReadAction
            public boolean descriptionCheck(@Nonnull LocalizeValue description, PsiElement highlightedElement) {
                return description != LocalizeValue.empty() && errorKey.get().equals(description.getKey().orElse(null));
            }

            @Override
            @RequiredReadAction
            public boolean accept(@Nonnull PsiElement highlightedElement) {
                return !BuilderHandler.isDefaultBuilderValue(highlightedElement);
            }
        },

        // field should have lazy getter and should be initialized in constructors
        //see com.intellij.java.lomboktest.LombokHighlightingTest.testGetterLazyInvocationProduceNPE
        METHOD_INVOCATION_WILL_PRODUCE_NPE(HighlightSeverity.WARNING, CodeInsightColors.WARNINGS_ATTRIBUTES) {
            private final CommonProblemDescriptor descriptor = new CommonProblemDescriptor() {
                @Nonnull
                @Override
                public LocalizeValue getDescriptionTemplate() {
                    return JavaAnalysisLocalize.dataflowMessageNpeMethodInvocationSure();
                }

                @Nonnull
                @Override
                public QuickFix[] getFixes() {
                    return QuickFix.EMPTY_ARRAY;
                }
            };

            @Override
            @RequiredReadAction
            public boolean descriptionCheck(@Nonnull LocalizeValue description, PsiElement highlightedElement) {
                return ProblemDescriptorUtil.renderDescriptionMessage(descriptor, highlightedElement).get().equals(description.get());
            }

            @Override
            @RequiredReadAction
            public boolean accept(@Nonnull PsiElement highlightedElement) {
                return !LazyGetterHandler.isLazyGetterHandled(highlightedElement)
                    || !LazyGetterHandler.isInitializedInConstructors(highlightedElement);
            }
        };

        private final HighlightSeverity severity;
        private final TextAttributesKey key;

        LombokHighlightFilter(@Nonnull HighlightSeverity severity, @Nullable TextAttributesKey key) {
            this.severity = severity;
            this.key = key;
        }

        /**
         * @param description        of the current highlighted element
         * @param highlightedElement the current highlighted element
         * @return true if the filter can handle current type of the highlight info with that kind of the description
         */
        @RequiredReadAction
        abstract public boolean descriptionCheck(@Nonnull LocalizeValue description, PsiElement highlightedElement);

        /**
         * @param highlightedElement the deepest element (it's the leaf element in PSI tree where the highlight was occurred)
         * @return false if the highlight should be suppressed
         */
        @RequiredReadAction
        abstract public boolean accept(@Nonnull PsiElement highlightedElement);
    }
}
