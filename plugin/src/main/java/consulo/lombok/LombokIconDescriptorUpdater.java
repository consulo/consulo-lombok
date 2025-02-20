package consulo.lombok;

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.icon.IconDescriptor;
import consulo.language.icon.IconDescriptorUpdater;
import consulo.language.psi.PsiElement;
import consulo.lombok.impl.icon.LombokIconGroup;
import de.plushnikov.intellij.plugin.psi.LombokLightClassBuilder;
import de.plushnikov.intellij.plugin.psi.LombokLightFieldBuilder;
import de.plushnikov.intellij.plugin.psi.LombokLightMethodBuilder;
import jakarta.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 22.05.2024
 */
@ExtensionImpl(id = "lombok", order = "after java")
public class LombokIconDescriptorUpdater implements IconDescriptorUpdater {
  @RequiredReadAction
  @Override
  public void updateIcon(@Nonnull IconDescriptor iconDescriptor, @Nonnull PsiElement element, int i) {
    if (element instanceof LombokLightClassBuilder) {
      iconDescriptor.setMainIcon(LombokIconGroup.nodesLombokclass());
    }
    else if (element instanceof LombokLightFieldBuilder) {
      iconDescriptor.setMainIcon(LombokIconGroup.nodesLombokfield());
    }
    else if (element instanceof LombokLightMethodBuilder) {
      iconDescriptor.setMainIcon(LombokIconGroup.nodesLombokmethod());
    }
  }
}
