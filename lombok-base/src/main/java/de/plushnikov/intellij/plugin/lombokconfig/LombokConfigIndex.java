package de.plushnikov.intellij.plugin.lombokconfig;

import consulo.annotation.component.ExtensionImpl;
import consulo.index.io.*;
import consulo.index.io.data.DataExternalizer;
import consulo.language.psi.stub.DefaultFileTypeSpecificInputFilter;
import consulo.language.psi.stub.FileBasedIndex;
import consulo.language.psi.stub.FileBasedIndexExtension;
import consulo.language.psi.stub.FileContent;
import consulo.util.collection.ContainerUtil;
import consulo.util.io.PathUtil;
import consulo.util.lang.Pair;
import consulo.util.lang.StringUtil;
import consulo.virtualFileSystem.VirtualFile;
import de.plushnikov.intellij.plugin.language.LombokConfigFileType;
import de.plushnikov.intellij.plugin.language.psi.LombokConfigCleaner;
import de.plushnikov.intellij.plugin.language.psi.LombokConfigFile;
import de.plushnikov.intellij.plugin.language.psi.LombokConfigProperty;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@ExtensionImpl
public class LombokConfigIndex extends FileBasedIndexExtension<ConfigKey, ConfigValue> {
  @NonNls
  public static final ID<ConfigKey, ConfigValue> NAME = ID.create("LombokConfigIndex");

  @NotNull
  @Override
  public ID<ConfigKey, ConfigValue> getName() {
    return NAME;
  }

  @NotNull
  @Override
  public DataIndexer<ConfigKey, ConfigValue, FileContent> getIndexer() {
    return new DataIndexer<>() {
      @NotNull
      @Override
      public Map<ConfigKey, ConfigValue> map(@NotNull FileContent inputData) {
        Map<ConfigKey, ConfigValue> result = Collections.emptyMap();

        final VirtualFile directoryFile = inputData.getFile().getParent();
        if (null != directoryFile) {
          final String canonicalPath = PathUtil.toSystemIndependentName(directoryFile.getCanonicalPath());
          if (null != canonicalPath) {
            final Map<String, String> configValues = extractValues((LombokConfigFile)inputData.getPsiFile());

            final boolean stopBubblingValue = Boolean.parseBoolean(configValues.get(StringUtil.toLowerCase(ConfigKey.CONFIG_STOP_BUBBLING.getConfigKey())));
            result = ContainerUtil.map2Map(ConfigKey.values(),
                                           key -> Pair.create(key,
                                                              new ConfigValue(configValues.get(StringUtil.toLowerCase(key.getConfigKey())), stopBubblingValue)));
          }
        }
        return result;
      }

      private static Map<String, String> extractValues(LombokConfigFile configFile) {
        Map<String, String> result = new HashMap<>();

        final LombokConfigCleaner[] configCleaners = LombokConfigUtil.getLombokConfigCleaners(configFile);
        for (LombokConfigCleaner configCleaner : configCleaners) {
          final String key = StringUtil.toLowerCase(configCleaner.getKey());

          final ConfigKey configKey = ConfigKey.fromConfigStringKey(key);
          if (null != configKey) {
            result.put(key, configKey.getConfigDefaultValue());
          }
        }

        final LombokConfigProperty[] configProperties = LombokConfigUtil.getLombokConfigProperties(configFile);
        for (LombokConfigProperty configProperty : configProperties) {
          final String key = StringUtil.toLowerCase(configProperty.getKey());
          final String value = configProperty.getValue();
          final String sign = configProperty.getSign();
          if (null == sign) {
            result.put(key, value);
          }
          else {
            final String previousValue = StringUtil.defaultIfEmpty(result.get(key), "");
            final String combinedValue = previousValue + sign + value + ";";
            result.put(key, combinedValue);
          }
        }

        return result;
      }
    };
  }

  @NotNull
  @Override
  public KeyDescriptor<ConfigKey> getKeyDescriptor() {
    return new EnumDataDescriptor<>(ConfigKey.class);
  }

  @NotNull
  @Override
  public DataExternalizer<ConfigValue> getValueExternalizer() {
    return new DataExternalizer<>() {
      @Override
      public void save(@NotNull DataOutput out, ConfigValue configValue) throws IOException {
        var isNotNullValue = configValue.getValue() != null;
        out.writeBoolean(isNotNullValue);
        if (isNotNullValue) {
          EnumeratorStringDescriptor.INSTANCE.save(out, configValue.getValue());
        }
        out.writeBoolean(configValue.isStopBubbling());
      }

      @Override
      public ConfigValue read(@NotNull DataInput in) throws IOException {
        var isNotNullValue = in.readBoolean();
        return new ConfigValue(isNotNullValue ? EnumeratorStringDescriptor.INSTANCE.read(in) : null, in.readBoolean());
      }
    };
  }

  @NotNull
  @Override
  public FileBasedIndex.InputFilter getInputFilter() {
    return new DefaultFileTypeSpecificInputFilter(LombokConfigFileType.INSTANCE);
  }

  @Override
  public boolean dependsOnFileContent() {
    return true;
  }

  @Override
  public int getVersion() {
    return 14;
  }
}
