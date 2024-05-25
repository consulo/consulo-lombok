package consulo.lombok.processor;

import consulo.application.Application;
import de.plushnikov.intellij.plugin.processor.Processor;
import jakarta.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 13/09/2023
 */
public class ProcessorUtil {
  @Nonnull
  public static <T extends Processor> T getProcessor(@Nonnull Class<T> processorClass) {
    return Application.get().getExtensionPoint(Processor.class).findExtensionOrFail(processorClass);
  }
}
