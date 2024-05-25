package de.plushnikov.intellij.plugin.extension;

import com.intellij.java.analysis.JavaAnalysisBundle;
import com.intellij.java.analysis.impl.codeInsight.intention.AddAnnotationFix;
import com.intellij.java.language.impl.codeInsight.daemon.JavaErrorBundle;
import com.intellij.java.language.psi.*;
import consulo.annotation.component.ExtensionImpl;
import consulo.codeEditor.CodeInsightColors;
import consulo.colorScheme.TextAttributesKey;
import consulo.java.impl.JavaBundle;
import consulo.language.editor.annotation.HighlightSeverity;
import consulo.language.editor.inspection.CommonProblemDescriptor;
import consulo.language.editor.inspection.ProblemDescriptorUtil;
import consulo.language.editor.inspection.QuickFix;
import consulo.language.editor.rawHighlight.HighlightInfo;
import consulo.language.editor.rawHighlight.HighlightInfoFilter;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.project.Project;
import de.plushnikov.intellij.plugin.LombokClassNames;
import de.plushnikov.intellij.plugin.handler.BuilderHandler;
import de.plushnikov.intellij.plugin.handler.FieldNameConstantsHandler;
import de.plushnikov.intellij.plugin.handler.LazyGetterHandler;
import de.plushnikov.intellij.plugin.handler.OnXAnnotationHandler;
import de.plushnikov.intellij.plugin.util.LombokLibraryUtil;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.*;
import java.util.regex.Pattern;

@ExtensionImpl
public class LombokHighlightErrorFilter implements HighlightInfoFilter {

  private static final class Holder {
    static final Pattern LOMBOK_ANY_ANNOTATION_REQUIRED =
      Pattern.compile(JavaErrorBundle.message("incompatible.types", "lombok.*AnyAnnotation\\[\\]", "__*"));

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
    String description = highlightInfo.getDescription();
    if (HighlightSeverity.ERROR.equals(highlightInfo.getSeverity())) {
      //Handling onX parameters
      if (OnXAnnotationHandler.isOnXParameterAnnotation(highlightInfo, file)
        || OnXAnnotationHandler.isOnXParameterValue(highlightInfo, file)
        || (description != null && Holder.LOMBOK_ANY_ANNOTATION_REQUIRED.matcher(description).matches())) {
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
      private final Pattern pattern = preparePattern(1);
      private final Pattern pattern2 = preparePattern(2);

      @Nonnull
      private static Pattern preparePattern(int count) {
        return Pattern.compile(JavaErrorBundle.message("unhandled.exceptions", ".*", count));
      }

      @Override
      public boolean descriptionCheck(@Nullable String description) {
        return description != null && (pattern.matcher(description).matches() || pattern2.matcher(description).matches());
      }

      @Override
      public void processHook(@Nonnull PsiElement highlightedElement, @Nonnull HighlightInfo highlightInfo) {
        PsiElement importantParent = PsiTreeUtil.getParentOfType(highlightedElement,
                                                                 PsiMethod.class,
                                                                 PsiLambdaExpression.class,
                                                                 PsiMethodReferenceExpression.class,
                                                                 PsiClassInitializer.class
        );

        // applicable only for methods
        if (importantParent instanceof PsiMethod) {
          AddAnnotationFix fix = new AddAnnotationFix(LombokClassNames.SNEAKY_THROWS, (PsiModifierListOwner)importantParent);
          highlightInfo.registerFix(fix, null, null, null, null);
        }
      }
    };

    private final HighlightSeverity severity;
    private final TextAttributesKey key;

    LombokHighlightFixHook(@Nonnull HighlightSeverity severity, @Nullable TextAttributesKey key) {
      this.severity = severity;
      this.key = key;
    }

    abstract public boolean descriptionCheck(@Nullable String description);

    abstract public void processHook(@Nonnull PsiElement highlightedElement, @Nonnull HighlightInfo highlightInfo);
  }

  private enum LombokHighlightFilter {
    // ERROR HANDLERS

    //see com.intellij.java.lomboktest.LombokHighlightingTest.testGetterLazyVariableNotInitialized
    VARIABLE_MIGHT_NOT_BEEN_INITIALIZED(HighlightSeverity.ERROR, CodeInsightColors.ERRORS_ATTRIBUTES) {
      @Override
      public boolean descriptionCheck(@Nullable String description, PsiElement highlightedElement) {
        return JavaErrorBundle.message("variable.not.initialized", highlightedElement.getText()).equals(description);
      }

      @Override
      public boolean accept(@Nonnull PsiElement highlightedElement) {
        return !LazyGetterHandler.isLazyGetterHandled(highlightedElement);
      }
    },

    //see com.intellij.java.lomboktest.LombokHighlightingTest.testFieldNameConstantsExample
    CONSTANT_EXPRESSION_REQUIRED(HighlightSeverity.ERROR, CodeInsightColors.ERRORS_ATTRIBUTES) {
      @Override
      public boolean descriptionCheck(@Nullable String description, PsiElement highlightedElement) {
        return JavaErrorBundle.message("constant.expression.required").equals(description);
      }

      @Override
      public boolean accept(@Nonnull PsiElement highlightedElement) {
        return !FieldNameConstantsHandler.isFiledNameConstants(highlightedElement);
      }
    },

    // WARNINGS HANDLERS
    //see com.intellij.java.lomboktest.LombokHighlightingTest.testBuilderWithDefaultRedundantInitializer
    VARIABLE_INITIALIZER_IS_REDUNDANT(HighlightSeverity.WARNING, CodeInsightColors.NOT_USED_ELEMENT_ATTRIBUTES) {
      private final Pattern pattern = Pattern.compile(
        JavaBundle.message("inspection.unused.assignment.problem.descriptor2", "(.+)", "(.+)"));

      @Override
      public boolean descriptionCheck(@Nullable String description, PsiElement highlightedElement) {
        return description != null && pattern.matcher(description).matches();
      }

      @Override
      public boolean accept(@Nonnull PsiElement highlightedElement) {
        return !BuilderHandler.isDefaultBuilderValue(highlightedElement);
      }
    },

    // field should have lazy getter and should be initialized in constructors
    //see com.intellij.java.lomboktest.LombokHighlightingTest.testGetterLazyInvocationProduceNPE
    METHOD_INVOCATION_WILL_PRODUCE_NPE(HighlightSeverity.WARNING, CodeInsightColors.WARNINGS_ATTRIBUTES) {
      private final CommonProblemDescriptor descriptor = new CommonProblemDescriptor() {
        @Override
        public @Nonnull String getDescriptionTemplate() {
          return JavaAnalysisBundle.message("dataflow.message.npe.method.invocation.sure");
        }

        @Override
        public @Nonnull QuickFix[] getFixes() {
          return null;
        }
      };

      @Override
      public boolean descriptionCheck(@Nullable String description, PsiElement highlightedElement) {
        return ProblemDescriptorUtil.renderDescriptionMessage(descriptor, highlightedElement).equals(description);
      }

      @Override
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
    abstract public boolean descriptionCheck(@Nullable String description, PsiElement highlightedElement);

    /**
     * @param highlightedElement the deepest element (it's the leaf element in PSI tree where the highlight was occurred)
     * @return false if the highlight should be suppressed
     */
    abstract public boolean accept(@Nonnull PsiElement highlightedElement);
  }
}
