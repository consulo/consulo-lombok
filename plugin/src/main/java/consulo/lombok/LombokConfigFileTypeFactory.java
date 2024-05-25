package consulo.lombok;

import consulo.annotation.component.ExtensionImpl;
import consulo.virtualFileSystem.fileType.FileNameMatcherFactory;
import consulo.virtualFileSystem.fileType.FileTypeConsumer;
import consulo.virtualFileSystem.fileType.FileTypeFactory;
import de.plushnikov.intellij.plugin.language.LombokConfigFileType;
import jakarta.annotation.Nonnull;
import jakarta.inject.Inject;

/**
 * @author VISTALL
 * @since 25.05.2024
 */
@ExtensionImpl
public class LombokConfigFileTypeFactory extends FileTypeFactory {
  private final FileNameMatcherFactory myFileNameMatcherFactory;

  @Inject
  public LombokConfigFileTypeFactory(FileNameMatcherFactory fileNameMatcherFactory) {
    myFileNameMatcherFactory = fileNameMatcherFactory;
  }

  @Override
  public void createFileTypes(@Nonnull FileTypeConsumer fileTypeConsumer) {
    fileTypeConsumer.consume(LombokConfigFileType.INSTANCE, myFileNameMatcherFactory.createExactFileNameMatcher("lombok.config"));
  }
}
