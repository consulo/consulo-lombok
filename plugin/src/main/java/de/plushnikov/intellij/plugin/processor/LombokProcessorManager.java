package de.plushnikov.intellij.plugin.processor;

import com.intellij.java.language.psi.PsiAnnotation;
import com.intellij.java.language.psi.PsiJavaCodeReferenceElement;
import consulo.language.psi.PsiElement;
import consulo.lombok.processor.ProcessorUtil;
import consulo.util.collection.ContainerUtil;
import consulo.util.lang.StringUtil;
import de.plushnikov.intellij.plugin.processor.clazz.*;
import de.plushnikov.intellij.plugin.processor.clazz.builder.*;
import de.plushnikov.intellij.plugin.processor.clazz.constructor.AllArgsConstructorProcessor;
import de.plushnikov.intellij.plugin.processor.clazz.constructor.NoArgsConstructorProcessor;
import de.plushnikov.intellij.plugin.processor.clazz.constructor.RequiredArgsConstructorProcessor;
import de.plushnikov.intellij.plugin.processor.clazz.fieldnameconstants.FieldNameConstantsOldProcessor;
import de.plushnikov.intellij.plugin.processor.clazz.fieldnameconstants.FieldNameConstantsPredefinedInnerClassFieldProcessor;
import de.plushnikov.intellij.plugin.processor.clazz.fieldnameconstants.FieldNameConstantsProcessor;
import de.plushnikov.intellij.plugin.processor.clazz.log.*;
import de.plushnikov.intellij.plugin.processor.field.*;
import de.plushnikov.intellij.plugin.processor.method.BuilderClassMethodProcessor;
import de.plushnikov.intellij.plugin.processor.method.BuilderMethodProcessor;
import de.plushnikov.intellij.plugin.processor.method.DelegateMethodProcessor;
import de.plushnikov.intellij.plugin.processor.modifier.*;
import jakarta.annotation.Nonnull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public final class LombokProcessorManager {

  private static final Map<String, Collection<Processor>> PROCESSOR_CACHE = new ConcurrentHashMap<>();

  private static Collection<Processor> getWithCache(String key, Supplier<Collection<Processor>> function) {
    return PROCESSOR_CACHE.computeIfAbsent(key, s -> function.get());
  }

  private static final Set<String> ourSupportedShortNames = getAllProcessors()
    .stream().flatMap(p -> Arrays.stream(p.getSupportedAnnotationClasses()))
    .map(StringUtil::getShortName)
    .collect(Collectors.toSet());

  @Nonnull
  private static Collection<Processor> getAllProcessors() {
    return Arrays.asList(
      ProcessorUtil.getProcessor(AllArgsConstructorProcessor.class),
      ProcessorUtil.getProcessor(NoArgsConstructorProcessor.class),
      ProcessorUtil.getProcessor(RequiredArgsConstructorProcessor.class),

      ProcessorUtil.getProcessor(LogProcessor.class),
      ProcessorUtil.getProcessor(Log4jProcessor.class),
      ProcessorUtil.getProcessor(Log4j2Processor.class),
      ProcessorUtil.getProcessor(Slf4jProcessor.class),
      ProcessorUtil.getProcessor(XSlf4jProcessor.class),
      ProcessorUtil.getProcessor(CommonsLogProcessor.class),
      ProcessorUtil.getProcessor(JBossLogProcessor.class),
      ProcessorUtil.getProcessor(FloggerProcessor.class),
      ProcessorUtil.getProcessor(CustomLogProcessor.class),

      ProcessorUtil.getProcessor(DataProcessor.class),
      ProcessorUtil.getProcessor(EqualsAndHashCodeProcessor.class),
      ProcessorUtil.getProcessor(GetterProcessor.class),
      ProcessorUtil.getProcessor(SetterProcessor.class),
      ProcessorUtil.getProcessor(ToStringProcessor.class),
      ProcessorUtil.getProcessor(WitherProcessor.class),

      ProcessorUtil.getProcessor(BuilderPreDefinedInnerClassFieldProcessor.class),
      ProcessorUtil.getProcessor(BuilderPreDefinedInnerClassMethodProcessor.class),
      ProcessorUtil.getProcessor(BuilderClassProcessor.class),
      ProcessorUtil.getProcessor(BuilderProcessor.class),
      ProcessorUtil.getProcessor(BuilderClassMethodProcessor.class),
      ProcessorUtil.getProcessor(BuilderMethodProcessor.class),

      ProcessorUtil.getProcessor(SuperBuilderPreDefinedInnerClassFieldProcessor.class),
      ProcessorUtil.getProcessor(SuperBuilderPreDefinedInnerClassMethodProcessor.class),
      ProcessorUtil.getProcessor(SuperBuilderClassProcessor.class),
      ProcessorUtil.getProcessor(SuperBuilderProcessor.class),

      ProcessorUtil.getProcessor(ValueProcessor.class),

      ProcessorUtil.getProcessor(UtilityClassProcessor.class),
      ProcessorUtil.getProcessor(StandardExceptionProcessor.class),

      ProcessorUtil.getProcessor(FieldNameConstantsOldProcessor.class),
      ProcessorUtil.getProcessor(FieldNameConstantsFieldProcessor.class),

      ProcessorUtil.getProcessor(FieldNameConstantsProcessor.class),
      ProcessorUtil.getProcessor(FieldNameConstantsPredefinedInnerClassFieldProcessor.class),

      ProcessorUtil.getProcessor(DelegateFieldProcessor.class),
      ProcessorUtil.getProcessor(GetterFieldProcessor.class),
      ProcessorUtil.getProcessor(SetterFieldProcessor.class),
      ProcessorUtil.getProcessor(WitherFieldProcessor.class),

      ProcessorUtil.getProcessor(DelegateMethodProcessor.class),

      ProcessorUtil.getProcessor(CleanupProcessor.class),
      ProcessorUtil.getProcessor(SynchronizedProcessor.class),
      ProcessorUtil.getProcessor(JacksonizedProcessor.class)
    );
  }

  @Nonnull
  public static Collection<ModifierProcessor> getLombokModifierProcessors() {
    return Arrays.asList(new FieldDefaultsModifierProcessor(),
                         new UtilityClassModifierProcessor(),
                         new ValModifierProcessor(),
                         new ValueModifierProcessor());
  }

  @Nonnull
  public static Collection<Processor> getProcessors(@Nonnull Class<? extends PsiElement> supportedClass) {
    return getWithCache("bySupportedClass_" + supportedClass.getName(),
                        () -> ContainerUtil.filter(getAllProcessors(), p -> p.isSupportedClass(supportedClass))
    );
  }

  @Nonnull
  public static Collection<Processor> getProcessors(@Nonnull PsiAnnotation psiAnnotation) {
    PsiJavaCodeReferenceElement nameReferenceElement = psiAnnotation.getNameReferenceElement();
    if (nameReferenceElement == null) {
      return Collections.emptyList();
    }
    String referenceName = nameReferenceElement.getReferenceName();
    if (referenceName == null || !ourSupportedShortNames.contains(referenceName)) {
      return Collections.emptyList();
    }
    final String qualifiedName = psiAnnotation.getQualifiedName();
    if (StringUtil.isEmpty(qualifiedName) || !qualifiedName.contains("lombok")) {
      return Collections.emptyList();
    }
    return getWithCache("byAnnotationFQN_" + qualifiedName,
                        () -> ContainerUtil.filter(getAllProcessors(), p -> p.isSupportedAnnotationFQN(qualifiedName))
    );
  }
}
