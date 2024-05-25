package de.plushnikov.intellij.plugin.processor;

import com.intellij.java.language.psi.PsiAnnotation;
import com.intellij.java.language.psi.PsiClass;
import com.intellij.java.language.psi.PsiField;
import com.intellij.java.language.psi.PsiModifierListOwner;
import consulo.language.psi.PsiElement;
import de.plushnikov.intellij.plugin.LombokClassNames;
import de.plushnikov.intellij.plugin.lombokconfig.ConfigDiscovery;
import de.plushnikov.intellij.plugin.lombokconfig.ConfigKey;
import de.plushnikov.intellij.plugin.util.PsiAnnotationSearchUtil;
import de.plushnikov.intellij.plugin.util.PsiAnnotationUtil;
import jakarta.annotation.Nonnull;

import java.util.Collection;

/**
 * Base lombok processor class
 *
 * @author Plushnikov Michail
 */
public abstract class AbstractProcessor implements Processor {
  /**
   * Annotation classes this processor supports
   */
  private final String[] supportedAnnotationClasses;
  /**
   * Kind of output elements this processor supports
   */
  private final Class<? extends PsiElement> supportedClass;
  /**
   * Instance of config discovery service to access lombok.config informations
   */
  protected final ConfigDiscovery configDiscovery;

  /**
   * Constructor for all Lombok-Processors
   *
   * @param supportedClass             kind of output elements this processor supports
   * @param supportedAnnotationClasses annotations this processor supports
   */
  protected AbstractProcessor(@Nonnull Class<? extends PsiElement> supportedClass,
                              @Nonnull String... supportedAnnotationClasses) {
    this.configDiscovery = ConfigDiscovery.getInstance();
    this.supportedClass = supportedClass;
    this.supportedAnnotationClasses = supportedAnnotationClasses;
  }

  @Override
  public final @Nonnull String[] getSupportedAnnotationClasses() {
    return supportedAnnotationClasses;
  }

  @Nonnull
  @Override
  public final Class<? extends PsiElement> getSupportedClass() {
    return supportedClass;
  }

  @Nonnull
  public abstract Collection<PsiAnnotation> collectProcessedAnnotations(@Nonnull PsiClass psiClass);

  protected boolean supportAnnotationVariant(@Nonnull PsiAnnotation psiAnnotation) {
    return true;
  }

  protected void filterToleratedElements(@Nonnull Collection<? extends PsiModifierListOwner> definedMethods) {
    definedMethods.removeIf(definedMethod -> PsiAnnotationSearchUtil.isAnnotatedWith(definedMethod, LombokClassNames.TOLERATE));
  }

  protected boolean readAnnotationOrConfigProperty(@Nonnull PsiAnnotation psiAnnotation, @Nonnull PsiClass psiClass,
                                                   @Nonnull String annotationParameter, @Nonnull ConfigKey configKey) {
    final boolean result;
    final Boolean declaredAnnotationValue = PsiAnnotationUtil.getDeclaredBooleanAnnotationValue(psiAnnotation, annotationParameter);
    if (null == declaredAnnotationValue) {
      result = configDiscovery.getBooleanLombokConfigProperty(configKey, psiClass);
    }
    else {
      result = declaredAnnotationValue;
    }
    return result;
  }

  @Override
  public LombokPsiElementUsage checkFieldUsage(@Nonnull PsiField psiField, @Nonnull PsiAnnotation psiAnnotation) {
    return LombokPsiElementUsage.NONE;
  }
}
