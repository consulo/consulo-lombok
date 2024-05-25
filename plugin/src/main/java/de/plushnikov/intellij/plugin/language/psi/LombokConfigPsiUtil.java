package de.plushnikov.intellij.plugin.language.psi;

import consulo.language.ast.ASTNode;
import consulo.language.ast.IElementType;
import consulo.util.lang.StringUtil;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public final class LombokConfigPsiUtil {
  public static String getKey(@Nonnull LombokConfigCleaner element) {
    return getNodeText(element.getNode(), LombokConfigTypes.KEY);
  }

  public static String getKey(@Nonnull LombokConfigProperty element) {
    return getNodeText(element.getNode(), LombokConfigTypes.KEY);
  }

  public static String getValue(@Nonnull LombokConfigProperty element) {
    return getNodeText(element.getNode(), LombokConfigTypes.VALUE);
  }

  public static String getSign(@Nonnull LombokConfigProperty element) {
    return StringUtil.trim(getNodeText(element.getOperation().getNode(), LombokConfigTypes.SIGN));
  }

  public static String getSign(@Nonnull LombokConfigOperation element) {
    return StringUtil.trim(getNodeText(element.getNode(), LombokConfigTypes.SIGN));
  }

  @Nullable
  private static String getNodeText(@Nonnull ASTNode node, @Nonnull IElementType type) {
    final ASTNode valueNode = node.findChildByType(type);
    if (valueNode != null) {
      return valueNode.getText();
    } else {
      return null;
    }
  }
}
