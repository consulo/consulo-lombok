package consulo.lombok.processor;

import consulo.application.Application;
import consulo.component.extension.ExtensionPoint;
import consulo.component.extension.ExtensionPointCacheKey;
import consulo.util.lang.StringUtil;
import de.plushnikov.intellij.plugin.processor.Processor;
import jakarta.annotation.Nonnull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * @author VISTALL
 * @since 2025-02-20
 */
public record ProcessorCacheData(Set<String> supportedClassNames,
                                 Map<String, Collection<Processor>> processorCache) {
  public static final ExtensionPointCacheKey<Processor, ProcessorCacheData> CACHE_KEY =
    ExtensionPointCacheKey.create("ProcessorCacheData", walker -> {
      Set<String> classNames = new HashSet<>();
      walker.walk(processor -> {
        for (String supportedAnnotationClass : processor.getSupportedAnnotationClasses()) {
          classNames.add(StringUtil.getShortName(supportedAnnotationClass));
        }
      });

      return new ProcessorCacheData(classNames, new ConcurrentHashMap<>());
    });

  @Nonnull
  public static Set<String> getSupportedClassNames(Application application) {
    ProcessorCacheData data = application.getExtensionPoint(Processor.class).getOrBuildCache(CACHE_KEY);
    return data.supportedClassNames();
  }

  @Nonnull
  public static Collection<Processor> getWithCache(Application application,
                                                   String key,
                                                   Function<List<Processor>, Collection<Processor>> function) {
    ExtensionPoint<Processor> point = application.getExtensionPoint(Processor.class);
    ProcessorCacheData data = point.getOrBuildCache(CACHE_KEY);
    return data.processorCache().computeIfAbsent(key, s -> function.apply(point.getExtensionList()));
  }
}
