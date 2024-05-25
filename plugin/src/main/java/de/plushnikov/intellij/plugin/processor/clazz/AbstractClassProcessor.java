package de.plushnikov.intellij.plugin.processor.clazz;

import com.intellij.java.language.psi.*;
import consulo.language.psi.PsiElement;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.util.lang.StringUtil;
import de.plushnikov.intellij.plugin.LombokClassNames;
import de.plushnikov.intellij.plugin.lombokconfig.ConfigKey;
import de.plushnikov.intellij.plugin.problem.LombokProblem;
import de.plushnikov.intellij.plugin.problem.ProblemProcessingSink;
import de.plushnikov.intellij.plugin.problem.ProblemSink;
import de.plushnikov.intellij.plugin.problem.ProblemValidationSink;
import de.plushnikov.intellij.plugin.processor.AbstractProcessor;
import de.plushnikov.intellij.plugin.psi.LombokLightClassBuilder;
import de.plushnikov.intellij.plugin.quickfix.PsiQuickFixFactory;
import de.plushnikov.intellij.plugin.thirdparty.LombokUtils;
import de.plushnikov.intellij.plugin.util.PsiAnnotationSearchUtil;
import de.plushnikov.intellij.plugin.util.PsiAnnotationUtil;
import de.plushnikov.intellij.plugin.util.PsiClassUtil;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Base lombok processor class for class annotations
 *
 * @author Plushnikov Michail
 */
public abstract class AbstractClassProcessor extends AbstractProcessor implements ClassProcessor {

  protected AbstractClassProcessor(@Nonnull Class<? extends PsiElement> supportedClass,
                                   @Nonnull String supportedAnnotationClass) {
    super(supportedClass, supportedAnnotationClass);
  }

  protected AbstractClassProcessor(@Nonnull Class<? extends PsiElement> supportedClass,
                                   @Nonnull String supportedAnnotationClass,
                                   @Nonnull String equivalentAnnotationClass) {
    super(supportedClass, supportedAnnotationClass, equivalentAnnotationClass);
  }

  @Nonnull
  @Override
  public List<? super PsiElement> process(@Nonnull PsiClass psiClass, @Nullable String nameHint) {
    List<? super PsiElement> result = Collections.emptyList();
    PsiAnnotation psiAnnotation = PsiAnnotationSearchUtil.findAnnotation(psiClass, getSupportedAnnotationClasses());
    if (null != psiAnnotation
      && supportAnnotationVariant(psiAnnotation)
      && possibleToGenerateElementNamed(nameHint, psiClass, psiAnnotation)
      && validate(psiAnnotation, psiClass, new ProblemProcessingSink())
    ) {
      result = new ArrayList<>();
      generatePsiElements(psiClass, psiAnnotation, result);
    }
    return result;
  }

  protected boolean possibleToGenerateElementNamed(@Nullable String nameHint, @Nonnull PsiClass psiClass,
                                                 @Nonnull PsiAnnotation psiAnnotation) {
    if (null == nameHint) {
      return true;
    }
    final Collection<String> namesOfGeneratedElements = getNamesOfPossibleGeneratedElements(psiClass, psiAnnotation);
    return namesOfGeneratedElements.isEmpty() || namesOfGeneratedElements.contains(nameHint);
  }

  protected Collection<String> getNamesOfPossibleGeneratedElements(@Nonnull PsiClass psiClass, @Nonnull PsiAnnotation psiAnnotation) {
    return Collections.emptyList();
  }

  @Nonnull
  @Override
  public Collection<PsiAnnotation> collectProcessedAnnotations(@Nonnull PsiClass psiClass) {
    Collection<PsiAnnotation> result = new ArrayList<>();
    PsiAnnotation psiAnnotation = PsiAnnotationSearchUtil.findAnnotation(psiClass, getSupportedAnnotationClasses());
    if (null != psiAnnotation) {
      result.add(psiAnnotation);
    }
    return result;
  }

  protected void addClassAnnotation(Collection<PsiAnnotation> result, @Nonnull PsiClass psiClass, String... annotationFQNs) {
    PsiAnnotation psiAnnotation = PsiAnnotationSearchUtil.findAnnotation(psiClass, annotationFQNs);
    if (null != psiAnnotation) {
      result.add(psiAnnotation);
    }
  }

  protected void addFieldsAnnotation(Collection<PsiAnnotation> result, @Nonnull PsiClass psiClass, String... annotationFQNs) {
    for (PsiField psiField : PsiClassUtil.collectClassFieldsIntern(psiClass)) {
      PsiAnnotation psiAnnotation = PsiAnnotationSearchUtil.findAnnotation(psiField, annotationFQNs);
      if (null != psiAnnotation) {
        result.add(psiAnnotation);
      }
    }
  }

  @Nonnull
  @Override
  public Collection<LombokProblem> verifyAnnotation(@Nonnull PsiAnnotation psiAnnotation) {
    Collection<LombokProblem> result = Collections.emptyList();
    // check first for fields, methods and filter it out, because PsiClass is parent of all annotations and will match other parents too
    PsiElement psiElement = PsiTreeUtil.getParentOfType(psiAnnotation, PsiField.class, PsiMethod.class, PsiClass.class);
    if (psiElement instanceof PsiClass) {
      ProblemValidationSink problemNewBuilder = new ProblemValidationSink();
      validate(psiAnnotation, (PsiClass) psiElement, problemNewBuilder);
      result = problemNewBuilder.getProblems();
    }

    return result;
  }

  protected Optional<PsiClass> getSupportedParentClass(@Nonnull PsiClass psiClass) {
    final PsiElement parentElement = psiClass.getParent();
    if (parentElement instanceof PsiClass && !(parentElement instanceof LombokLightClassBuilder)) {
      return Optional.of((PsiClass) parentElement);
    }
    return Optional.empty();
  }

  @Nullable
  protected PsiAnnotation getSupportedAnnotation(@Nonnull PsiClass psiParentClass) {
    return PsiAnnotationSearchUtil.findAnnotation(psiParentClass, getSupportedAnnotationClasses());
  }

  protected abstract boolean validate(@Nonnull PsiAnnotation psiAnnotation, @Nonnull PsiClass psiClass, @Nonnull ProblemSink builder);

  protected abstract void generatePsiElements(@Nonnull PsiClass psiClass, @Nonnull PsiAnnotation psiAnnotation, @Nonnull List<? super PsiElement> target);

  static void validateOfParam(PsiClass psiClass, ProblemSink builder, PsiAnnotation psiAnnotation, Collection<String> ofProperty) {
    for (String fieldName : ofProperty) {
      if (!StringUtil.isEmptyOrSpaces(fieldName)) {
        PsiField fieldByName = psiClass.findFieldByName(fieldName, false);
        if (null == fieldByName) {
          final String newPropertyValue = calcNewPropertyValue(ofProperty, fieldName);
          builder.addWarningMessage("inspection.message.field.s.does.not.exist.field", fieldName)
            .withLocalQuickFixes(() -> PsiQuickFixFactory.createChangeAnnotationParameterFix(psiAnnotation, "of", newPropertyValue));
        }
      }
    }
  }

  static void validateExcludeParam(PsiClass psiClass,
                                   ProblemSink builder,
                                   PsiAnnotation psiAnnotation,
                                   Collection<String> excludeProperty) {
    for (String fieldName : excludeProperty) {
      if (!StringUtil.isEmptyOrSpaces(fieldName)) {
        PsiField fieldByName = psiClass.findFieldByName(fieldName, false);
        if (null == fieldByName) {
          final String newPropertyValue = calcNewPropertyValue(excludeProperty, fieldName);
          builder.addWarningMessage("inspection.message.field.s.does.not.exist.exclude", fieldName)
            .withLocalQuickFixes(() -> PsiQuickFixFactory.createChangeAnnotationParameterFix(psiAnnotation, "exclude", newPropertyValue));
        } else {
          if (fieldName.startsWith(LombokUtils.LOMBOK_INTERN_FIELD_MARKER) || fieldByName.hasModifierProperty(PsiModifier.STATIC)) {
            final String newPropertyValue = calcNewPropertyValue(excludeProperty, fieldName);
            builder.addWarningMessage("inspection.message.field.s.would.have.been.excluded.anyway", fieldName)
              .withLocalQuickFixes(() -> PsiQuickFixFactory.createChangeAnnotationParameterFix(psiAnnotation, "exclude", newPropertyValue));
          }
        }
      }
    }
  }

  private static String calcNewPropertyValue(Collection<String> allProperties, String fieldName) {
    String result = null;
    if (!allProperties.isEmpty() && (allProperties.size() > 1 || !allProperties.contains(fieldName))) {
      result = allProperties.stream().filter(((Predicate<String>) fieldName::equals).negate())
        .collect(Collectors.joining("\",\"", "{\"", "\"}"));
    }
    return result;
  }

  boolean shouldGenerateExtraNoArgsConstructor(@Nonnull PsiClass psiClass) {
    boolean result = !PsiClassUtil.hasSuperClass(psiClass);
    if (result) {
      result = configDiscovery.getBooleanLombokConfigProperty(ConfigKey.NO_ARGS_CONSTRUCTOR_EXTRA_PRIVATE, psiClass);
    }
    if (result) {
      result = PsiAnnotationSearchUtil.isNotAnnotatedWith(psiClass, LombokClassNames.NO_ARGS_CONSTRUCTOR, LombokClassNames.ALL_ARGS_CONSTRUCTOR,
        LombokClassNames.REQUIRED_ARGS_CONSTRUCTOR);
    }
    return result;
  }

  boolean readCallSuperAnnotationOrConfigProperty(@Nonnull PsiAnnotation psiAnnotation, @Nonnull PsiClass psiClass, @Nonnull ConfigKey configKey) {
    final boolean result;
    final Boolean declaredAnnotationValue = PsiAnnotationUtil.getDeclaredBooleanAnnotationValue(psiAnnotation, "callSuper");
    if (null == declaredAnnotationValue) {
      final String configProperty = configDiscovery.getStringLombokConfigProperty(configKey, psiClass);
      result = PsiClassUtil.hasSuperClass(psiClass) && "CALL".equalsIgnoreCase(configProperty);
    } else {
      result = declaredAnnotationValue;
    }
    return result;
  }
}
