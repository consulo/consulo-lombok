package de.plushnikov.intellij.plugin.language;

import consulo.annotation.component.ExtensionImpl;
import consulo.colorScheme.TextAttributesKey;
import consulo.colorScheme.setting.AttributesDescriptor;
import consulo.colorScheme.setting.ColorDescriptor;
import consulo.language.editor.colorScheme.setting.ColorSettingsPage;
import consulo.language.editor.highlight.SyntaxHighlighter;
import de.plushnikov.intellij.plugin.LombokBundle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

  @NotNull
  @Override
  public SyntaxHighlighter getHighlighter() {
    return new LombokConfigSyntaxHighlighter();
  }

  @NotNull
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
  public AttributesDescriptor @NotNull [] getAttributeDescriptors() {
    return DESCRIPTORS;
  }

  @Override
  public ColorDescriptor @NotNull [] getColorDescriptors() {
    return ColorDescriptor.EMPTY_ARRAY;
  }

  @NotNull
  @Override
  public String getDisplayName() {
    return LombokBundle.message("configurable.name.lombok.config");
  }
}
