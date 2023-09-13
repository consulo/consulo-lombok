package de.plushnikov.intellij.plugin.lombokconfig;

import com.intellij.java.language.psi.PsiClass;
import consulo.application.ApplicationManager;
import consulo.application.util.CachedValueProvider;
import consulo.application.util.CachedValuesManager;
import consulo.application.util.ConcurrentFactoryMap;
import consulo.language.psi.PsiFile;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.language.psi.scope.GlobalSearchScopesCore;
import consulo.language.psi.stub.FileBasedIndex;
import consulo.language.psi.util.LanguageCachedValueUtil;
import consulo.project.Project;
import consulo.util.lang.StringUtil;
import consulo.virtualFileSystem.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ConfigDiscovery {
  @NotNull
  public static ConfigDiscovery getInstance() {
    return ApplicationManager.getApplication().getService(ConfigDiscovery.class);
  }

  public @NotNull LombokNullAnnotationLibrary getAddNullAnnotationLombokConfigProperty(@NotNull PsiClass psiClass) {
    final String configProperty = getStringLombokConfigProperty(ConfigKey.ADD_NULL_ANNOTATIONS, psiClass);
    if (StringUtil.isNotEmpty(configProperty)) {
      for (LombokNullAnnotationLibraryDefned library : LombokNullAnnotationLibraryDefned.values()) {
        if (library.getKey().equalsIgnoreCase(configProperty)) {
          return library;
        }
      }

      final LombokNullAnnotationLibrary parsedCustom = LombokNullAnnotationLibraryCustom.parseCustom(configProperty);
      if (null != parsedCustom) {
        return parsedCustom;
      }
    }
    return LombokNullAnnotationLibraryDefned.NONE;
  }

  public @NotNull Collection<String> getMultipleValueLombokConfigProperty(@NotNull ConfigKey configKey, @NotNull PsiClass psiClass) {
    return getConfigProperty(configKey, psiClass);
  }

  @NotNull
  public String getStringLombokConfigProperty(@NotNull ConfigKey configKey, @NotNull PsiClass psiClass) {
    Collection<String> result = getConfigProperty(configKey, psiClass);
    if (!result.isEmpty()) {
      return result.iterator().next();
    }
    return configKey.getConfigDefaultValue();
  }

  public boolean getBooleanLombokConfigProperty(@NotNull ConfigKey configKey, @NotNull PsiClass psiClass) {
    final String configProperty = getStringLombokConfigProperty(configKey, psiClass);
    return Boolean.parseBoolean(configProperty);
  }

  @NotNull
  private Collection<String> getConfigProperty(@NotNull ConfigKey configKey, @NotNull PsiClass psiClass) {
    @Nullable PsiFile psiFile = calculatePsiFile(psiClass);
    if (psiFile != null) {
      return discoverPropertyWithCache(configKey, psiFile);
    }
    return Collections.singletonList(configKey.getConfigDefaultValue());
  }

  @Nullable
  private static PsiFile calculatePsiFile(@NotNull PsiClass psiClass) {
    PsiFile psiFile = psiClass.getContainingFile();
    if (psiFile != null) {
      psiFile = psiFile.getOriginalFile();
    }
    return psiFile;
  }

  @NotNull
  protected Collection<String> discoverPropertyWithCache(@NotNull ConfigKey configKey,
                                                         @NotNull PsiFile psiFile) {
    return LanguageCachedValueUtil.getCachedValue(psiFile, () -> {
      Map<ConfigKey, Collection<String>> result =
        ConcurrentFactoryMap.createMap(configKeyInner -> discoverProperty(configKeyInner, psiFile));
      return CachedValueProvider.Result.create(result, LombokConfigChangeListener.CONFIG_CHANGE_TRACKER);
    }).get(configKey);
  }

  @NotNull
  protected Collection<String> discoverProperty(@NotNull ConfigKey configKey, @NotNull PsiFile psiFile) {
    if (configKey.isConfigScalarValue()) {
      return discoverScalarProperty(configKey, psiFile);
    }
    return discoverCollectionProperty(configKey, psiFile);
  }

  @NotNull
  private Collection<String> discoverScalarProperty(@NotNull ConfigKey configKey, @NotNull PsiFile psiFile) {
    @Nullable VirtualFile currentFile = psiFile.getVirtualFile();
    while (currentFile != null) {
      ConfigValue configValue = readProperty(configKey, psiFile.getProject(), currentFile);
      if (null != configValue) {
        if (null == configValue.getValue()) {
          if (configValue.isStopBubbling()) {
            break;
          }
        }
        else {
          return Collections.singletonList(configValue.getValue());
        }
      }

      currentFile = currentFile.getParent();
    }

    return Collections.singletonList(configKey.getConfigDefaultValue());
  }

  protected FileBasedIndex getFileBasedIndex() {
    return FileBasedIndex.getInstance();
  }

  @Nullable
  private ConfigValue readProperty(@NotNull ConfigKey configKey, @NotNull Project project, @NotNull VirtualFile directory) {
    GlobalSearchScope directoryScope = GlobalSearchScopesCore.directoryScope(project, directory, false);
    List<ConfigValue> values = getFileBasedIndex().getValues(LombokConfigIndex.NAME, configKey, directoryScope);
    if (!values.isEmpty()) {
      return values.iterator().next();
    }
    return null;
  }

  @NotNull
  private Collection<String> discoverCollectionProperty(@NotNull ConfigKey configKey, @NotNull PsiFile file) {
    List<String> properties = new ArrayList<>();

    final Project project = file.getProject();
    @Nullable VirtualFile currentFile = file.getVirtualFile();
    while (currentFile != null) {
      final ConfigValue configValue = readProperty(configKey, project, currentFile);
      if (null != configValue) {
        if (null == configValue.getValue()) {
          if (configValue.isStopBubbling()) {
            break;
          }
        }
        else {
          properties.add(configValue.getValue());
        }
      }

      currentFile = currentFile.getParent();
    }

    Collections.reverse(properties);

    Set<String> result = new HashSet<>();

    for (String configProperty : properties) {
      if (StringUtil.isNotEmpty(configProperty)) {
        final String[] values = configProperty.split(";");
        for (String value : values) {
          if (value.startsWith("+")) {
            result.add(value.substring(1));
          }
          else if (value.startsWith("-")) {
            result.remove(value.substring(1));
          }
        }
      }
    }

    return result;
  }
}
