package de.plushnikov.intellij.plugin.action.delombok;

import consulo.lombok.processor.ProcessorUtil;
import de.plushnikov.intellij.plugin.processor.clazz.builder.BuilderClassProcessor;
import de.plushnikov.intellij.plugin.processor.clazz.builder.BuilderPreDefinedInnerClassFieldProcessor;
import de.plushnikov.intellij.plugin.processor.clazz.builder.BuilderPreDefinedInnerClassMethodProcessor;
import de.plushnikov.intellij.plugin.processor.clazz.builder.BuilderProcessor;
import de.plushnikov.intellij.plugin.processor.method.BuilderClassMethodProcessor;
import de.plushnikov.intellij.plugin.processor.method.BuilderMethodProcessor;
import jakarta.annotation.Nonnull;

public class DelombokBuilderAction extends AbstractDelombokAction {

  @Override
  @Nonnull
  protected DelombokHandler createHandler() {
    return new DelombokHandler(true,
                               ProcessorUtil.getProcessor(BuilderPreDefinedInnerClassFieldProcessor.class),
                               ProcessorUtil.getProcessor(BuilderPreDefinedInnerClassMethodProcessor.class),
                               ProcessorUtil.getProcessor(BuilderClassProcessor.class),
                               ProcessorUtil.getProcessor(BuilderClassMethodProcessor.class),
                               ProcessorUtil.getProcessor(BuilderMethodProcessor.class),
                               ProcessorUtil.getProcessor(BuilderProcessor.class));
  }
}
