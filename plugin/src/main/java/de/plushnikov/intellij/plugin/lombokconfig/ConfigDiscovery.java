package de.plushnikov.intellij.plugin.lombokconfig;

import com.intellij.java.language.psi.PsiClass;
import consulo.annotation.component.ComponentScope;
import consulo.annotation.component.ServiceAPI;
import consulo.annotation.component.ServiceImpl;
import consulo.application.ApplicationManager;
import consulo.application.util.CachedValueProvider;
import consulo.application.util.ConcurrentFactoryMap;
import consulo.language.psi.PsiFile;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.language.psi.scope.GlobalSearchScopesCore;
import consulo.language.psi.stub.FileBasedIndex;
import consulo.language.psi.util.LanguageCachedValueUtil;
import consulo.project.Project;
import consulo.util.lang.StringUtil;
import consulo.virtualFileSystem.VirtualFile;
import jakarta.annotation.Nonnull;
import jakarta.inject.Singleton;
import jakarta.annotation.Nullable;

import java.util.*;

@Singleton
@ServiceAPI(ComponentScope.APPLICATION)
@ServiceImpl
public class ConfigDiscovery {
  @Nonnull
  public static ConfigDiscovery getInstance() {
    return ApplicationManager.getApplication().getInstance(ConfigDiscovery.class);
  }

  public @Nonnull LombokNullAnnotationLibrary getAddNullAnnotationLombokConfigProperty(@Nonnull PsiClass psiClass) {
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

  public @Nonnull Collection<String> getMultipleValueLombokConfigProperty(@Nonnull ConfigKey configKey, @Nonnull PsiClass psiClass) {
    return getConfigProperty(configKey, psiClass);
  }

  @Nonnull
  public String getStringLombokConfigProperty(@Nonnull ConfigKey configKey, @Nonnull PsiClass psiClass) {
    Collection<String> result = getConfigProperty(configKey, psiClass);
    if (!result.isEmpty()) {
      return result.iterator().next();
    }
    return configKey.getConfigDefaultValue();
  }

  public boolean getBooleanLombokConfigProperty(@Nonnull ConfigKey configKey, @Nonnull PsiClass psiClass) {
    final String configProperty = getStringLombokConfigProperty(configKey, psiClass);
    return Boolean.parseBoolean(configProperty);
  }

  @Nonnull
  private Collection<String> getConfigProperty(@Nonnull ConfigKey configKey, @Nonnull PsiClass psiClass) {
    @Nullable PsiFile psiFile = calculatePsiFile(psiClass);
    if (psiFile != null) {
      return discoverPropertyWithCache(configKey, psiFile);
    }
    return Collections.singletonList(configKey.getConfigDefaultValue());
  }

  @Nullable
  private static PsiFile calculatePsiFile(@Nonnull PsiClass psiClass) {
    PsiFile psiFile = psiClass.getContainingFile();
    if (psiFile != null) {
      psiFile = psiFile.getOriginalFile();
    }
    return psiFile;
  }

  @Nonnull
  protected Collection<String> discoverPropertyWithCache(@Nonnull ConfigKey configKey,
                                                         @Nonnull PsiFile psiFile) {
    return LanguageCachedValueUtil.getCachedValue(psiFile, () -> {
      Map<ConfigKey, Collection<String>> result =
        ConcurrentFactoryMap.createMap(configKeyInner -> discoverProperty(configKeyInner, psiFile));
      return CachedValueProvider.Result.create(result, LombokConfigChangeListener.CONFIG_CHANGE_TRACKER);
    }).get(configKey);
  }

  @Nonnull
  protected Collection<String> discoverProperty(@Nonnull ConfigKey configKey, @Nonnull PsiFile psiFile) {
    if (configKey.isConfigScalarValue()) {
      return discoverScalarProperty(configKey, psiFile);
    }
    return discoverCollectionProperty(configKey, psiFile);
  }

  @Nonnull
  private Collection<String> discoverScalarProperty(@Nonnull ConfigKey configKey, @Nonnull PsiFile psiFile) {
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
  private ConfigValue readProperty(@Nonnull ConfigKey configKey, @Nonnull Project project, @Nonnull VirtualFile directory) {
    GlobalSearchScope directoryScope = GlobalSearchScopesCore.directoryScope(project, directory, false);
    List<ConfigValue> values = getFileBasedIndex().getValues(LombokConfigIndex.NAME, configKey, directoryScope);
    if (!values.isEmpty()) {
      return values.iterator().next();
    }
    return null;
  }

  @Nonnull
  private Collection<String> discoverCollectionProperty(@Nonnull ConfigKey configKey, @Nonnull PsiFile file) {
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
