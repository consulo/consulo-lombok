package de.plushnikov.intellij.plugin.language;

import consulo.annotation.component.ExtensionImpl;
import consulo.colorScheme.TextAttributesKey;
import consulo.colorScheme.setting.AttributesDescriptor;
import consulo.language.editor.colorScheme.setting.ColorSettingsPage;
import consulo.language.editor.highlight.SyntaxHighlighter;
import de.plushnikov.intellij.plugin.LombokBundle;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.Map;

@ExtensionImpl
public class LombokConfigColorSettingsPage implements ColorSettingsPage {
  private static final AttributesDescriptor[] DESCRIPTORS = new AttributesDescriptor[]{
    new AttributesDescriptor(LombokBundle.message("color.settings.comment"), LombokConfigSyntaxHighlighter.COMMENT),
    new AttributesDescriptor(LombokBundle.message("color.settings.clear"), LombokConfigSyntaxHighlighter.CLEAR),
    new AttributesDescriptor(LombokBundle.message("color.settings.key"), LombokConfigSyntaxHighlighter.KEY),
    new AttributesDescriptor(LombokBundle.message("color.settings.separator"), LombokConfigSyntaxHighlighter.SEPARATOR),
    new AttributesDescriptor(LombokBundle.message("color.settings.value"), LombokConfigSyntaxHighlighter.VALUE),
  };

  @Nonnull
  @Override
  public SyntaxHighlighter getHighlighter() {
    return new LombokConfigSyntaxHighlighter();
  }

  @Nonnull
  @Override
  public String getDemoText() {
    return """
      ##
      ## Key : lombok.log.fieldName
      ## Type: string
      ##
      ## Use this name for the generated logger fields (default: 'log')
      ##
      ## Examples:
      #
      clear lombok.log.fieldName
      lombok.log.fieldName = LOGGER
      """;
  }

  @Nullable
  @Override
  public Map<String, TextAttributesKey> getAdditionalHighlightingTagToDescriptorMap() {
    return null;
  }

  @Override
  @Nonnull
  public AttributesDescriptor[] getAttributeDescriptors() {
    return DESCRIPTORS;
  }

  @Nonnull
  @Override
  public String getDisplayName() {
    return LombokBundle.message("configurable.name.lombok.config");
  }
}
