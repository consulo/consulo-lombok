package de.plushnikov.intellij.plugin.action.delombok;

import consulo.lombok.processor.ProcessorUtil;
import de.plushnikov.intellij.plugin.processor.clazz.builder.SuperBuilderClassProcessor;
import de.plushnikov.intellij.plugin.processor.clazz.builder.SuperBuilderPreDefinedInnerClassFieldProcessor;
import de.plushnikov.intellij.plugin.processor.clazz.builder.SuperBuilderPreDefinedInnerClassMethodProcessor;
import de.plushnikov.intellij.plugin.processor.clazz.builder.SuperBuilderProcessor;
import jakarta.annotation.Nonnull;

public class DelombokSuperBuilderAction extends AbstractDelombokAction {

  @Override
  @Nonnull
  protected DelombokHandler createHandler() {
    return new DelombokHandler(true,
                               ProcessorUtil.getProcessor(SuperBuilderPreDefinedInnerClassFieldProcessor.class),
                               ProcessorUtil.getProcessor(SuperBuilderPreDefinedInnerClassMethodProcessor.class),
                               ProcessorUtil.getProcessor(SuperBuilderClassProcessor.class),
                               ProcessorUtil.getProcessor(SuperBuilderProcessor.class));
  }
}
