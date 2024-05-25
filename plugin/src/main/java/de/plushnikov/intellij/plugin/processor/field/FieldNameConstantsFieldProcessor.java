package de.plushnikov.intellij.plugin.processor.field;

import com.intellij.java.language.psi.*;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiManager;
import consulo.language.psi.scope.GlobalSearchScope;
import de.plushnikov.intellij.plugin.LombokClassNames;
import de.plushnikov.intellij.plugin.lombokconfig.ConfigDiscovery;
import de.plushnikov.intellij.plugin.lombokconfig.ConfigKey;
import de.plushnikov.intellij.plugin.problem.ProblemSink;
import de.plushnikov.intellij.plugin.processor.LombokPsiElementUsage;
import de.plushnikov.intellij.plugin.psi.LombokLightFieldBuilder;
import de.plushnikov.intellij.plugin.thirdparty.LombokUtils;
import de.plushnikov.intellij.plugin.util.LombokProcessorUtil;
import de.plushnikov.intellij.plugin.util.PsiAnnotationUtil;
import jakarta.annotation.Nonnull;

import java.util.Collection;
import java.util.List;

/**
 * Inspect and validate @FieldNameConstants lombok annotation on a field
 * Creates string constants containing the field name for each field
 * Used for lombok v1.16.22 to lombok v1.18.2 only!
 *
 * @author Plushnikov Michail
 */
@ExtensionImpl
public final class FieldNameConstantsFieldProcessor extends AbstractFieldProcessor {
  private static final String CONFIG_DEFAULT = " CONFIG DEFAULT ";

  public FieldNameConstantsFieldProcessor() {
    super(PsiField.class, LombokClassNames.FIELD_NAME_CONSTANTS);
  }

  @Override
  protected boolean supportAnnotationVariant(@Nonnull PsiAnnotation psiAnnotation) {
    // old version of @FieldNameConstants has attributes "prefix" and "suffix", the new one not
    return null != psiAnnotation.findAttributeValue("prefix");
  }

  @Override
  protected Collection<String> getNamesOfPossibleGeneratedElements(@Nonnull PsiClass psiClass,
                                                                   @Nonnull PsiAnnotation psiAnnotation,
                                                                   @Nonnull PsiField psiField) {
    final String generatedElementName = calcFieldConstantName(psiField, psiAnnotation, psiClass);
    return List.of(generatedElementName);
  }

  @Override
  protected boolean validate(@Nonnull PsiAnnotation psiAnnotation, @Nonnull PsiField psiField, @Nonnull ProblemSink builder) {
    return LombokProcessorUtil.isLevelVisible(psiAnnotation) && checkIfFieldNameIsValidAndWarn(psiAnnotation, psiField, builder);
  }

  public static boolean checkIfFieldNameIsValidAndWarn(@Nonnull PsiAnnotation psiAnnotation,
                                                       @Nonnull PsiField psiField,
                                                       @Nonnull ProblemSink builder) {
    final boolean isValid = isValidFieldNameConstant(psiAnnotation, psiField);
    if (!isValid) {
      builder.addWarningMessage("inspection.message.not.generating.constant");
    }
    return isValid;
  }

  private static boolean isValidFieldNameConstant(@Nonnull PsiAnnotation psiAnnotation, @Nonnull PsiField psiField) {
    final PsiClass psiClass = psiField.getContainingClass();
    if (null != psiClass) {
      final String fieldName = calcFieldConstantName(psiField, psiAnnotation, psiClass);
      return !fieldName.equals(psiField.getName());
    }
    return false;
  }

  @Override
  protected void generatePsiElements(@Nonnull PsiField psiField, @Nonnull PsiAnnotation psiAnnotation, @Nonnull List<? super PsiElement> target) {
    final PsiClass psiClass = psiField.getContainingClass();
    if (null != psiClass) {
      target.add(createFieldNameConstant(psiField, psiClass, psiAnnotation));
    }
  }

  @Nonnull
  public static PsiField createFieldNameConstant(@Nonnull PsiField psiField,
                                                 @Nonnull PsiClass psiClass,
                                                 @Nonnull PsiAnnotation psiAnnotation) {
    final PsiManager manager = psiClass.getContainingFile().getManager();
    final PsiType psiFieldType = PsiType.getJavaLangString(manager, GlobalSearchScope.allScope(psiClass.getProject()));

    final String fieldModifier = LombokProcessorUtil.getLevelVisibility(psiAnnotation);
    final String fieldName = calcFieldConstantName(psiField, psiAnnotation, psiClass);

    LombokLightFieldBuilder fieldNameConstant = new LombokLightFieldBuilder(manager, fieldName, psiFieldType)
      .withContainingClass(psiClass)
      .withNavigationElement(psiField)
      .withModifier(PsiModifier.STATIC)
      .withModifier(PsiModifier.FINAL);
    if (!PsiModifier.PACKAGE_LOCAL.equals(fieldModifier)) {
      fieldNameConstant.withModifier(fieldModifier);
    }

    final PsiElementFactory psiElementFactory = JavaPsiFacade.getElementFactory(psiClass.getProject());
    final PsiExpression initializer = psiElementFactory.createExpressionFromText("\"" + psiField.getName() + "\"", psiClass);
    fieldNameConstant.setInitializer(initializer);
    return fieldNameConstant;
  }

  @Nonnull
  private static String calcFieldConstantName(@Nonnull PsiField psiField, @Nonnull PsiAnnotation psiAnnotation, @Nonnull PsiClass psiClass) {
    String prefix = PsiAnnotationUtil.getStringAnnotationValue(psiAnnotation, "prefix", CONFIG_DEFAULT);
    String suffix = PsiAnnotationUtil.getStringAnnotationValue(psiAnnotation, "suffix", CONFIG_DEFAULT);

    final ConfigDiscovery configDiscovery = ConfigDiscovery.getInstance();
    if (CONFIG_DEFAULT.equals(prefix)) {
      prefix = configDiscovery.getStringLombokConfigProperty(ConfigKey.FIELD_NAME_CONSTANTS_PREFIX, psiClass);
    }
    if (CONFIG_DEFAULT.equals(suffix)) {
      suffix = configDiscovery.getStringLombokConfigProperty(ConfigKey.FIELD_NAME_CONSTANTS_SUFFIX, psiClass);
    }

    return prefix + LombokUtils.camelCaseToConstant(psiField.getName()) + suffix;
  }

  @Override
  public LombokPsiElementUsage checkFieldUsage(@Nonnull PsiField psiField, @Nonnull PsiAnnotation psiAnnotation) {
    return LombokPsiElementUsage.USAGE;
  }
}
