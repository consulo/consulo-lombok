package de.plushnikov.intellij.plugin.resolver;

import com.intellij.java.impl.codeInsight.daemon.quickFix.ExternalLibraryResolver;
import com.intellij.java.language.projectRoots.roots.ExternalLibraryDescriptor;
import consulo.annotation.component.ExtensionImpl;
import consulo.module.Module;
import consulo.util.lang.StringUtil;
import consulo.util.lang.ThreeState;
import de.plushnikov.intellij.plugin.Version;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static de.plushnikov.intellij.plugin.LombokClassNames.MAIN_LOMBOK_CLASSES;

@ExtensionImpl
public class LombokExternalLibraryResolver extends ExternalLibraryResolver {

  private final Set<String> allLombokPackages;
  private final Map<String, String> simpleNameToFQNameMap;

  private static final ExternalLibraryDescriptor LOMBOK_DESCRIPTOR = new ExternalLibraryDescriptor("org.projectlombok",
                                                                                                   "lombok",
                                                                                                   null,
                                                                                                   Version.LAST_LOMBOK_VERSION) {
    @Nonnull
    @Override
    public List<String> getLibraryClassesRoots() {
      return List.of();
    }
  };

  public LombokExternalLibraryResolver() {
    allLombokPackages = MAIN_LOMBOK_CLASSES.stream().map(StringUtil::getPackageName).collect(Collectors.toUnmodifiableSet());
    simpleNameToFQNameMap = MAIN_LOMBOK_CLASSES.stream().collect(Collectors.toMap(StringUtil::getShortName, Function.identity()));
  }

  @Nullable
  @Override
  public ExternalClassResolveResult resolveClass(@Nonnull String shortClassName,
                                                 @Nonnull ThreeState isAnnotation,
                                                 @Nonnull Module contextModule) {
    if (isAnnotation == ThreeState.YES && simpleNameToFQNameMap.containsKey(shortClassName)) {
      return new ExternalClassResolveResult(simpleNameToFQNameMap.get(shortClassName), LOMBOK_DESCRIPTOR);
    }
    return null;
  }

  @Nullable
  @Override
  public ExternalLibraryDescriptor resolvePackage(@Nonnull String packageName) {
    if (allLombokPackages.contains(packageName)) {
      return LOMBOK_DESCRIPTOR;
    }
    return null;
  }
}
