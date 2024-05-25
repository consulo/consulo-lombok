package de.plushnikov.intellij.plugin.lombokconfig;

import consulo.annotation.component.ComponentScope;
import consulo.annotation.component.TopicImpl;
import consulo.component.util.ModificationTracker;
import consulo.util.lang.StringUtil;
import consulo.virtualFileSystem.VirtualFile;
import consulo.virtualFileSystem.event.BulkFileListener;
import consulo.virtualFileSystem.event.VFileEvent;
import consulo.virtualFileSystem.fileType.FileType;
import consulo.virtualFileSystem.fileType.FileTypeRegistry;
import de.plushnikov.intellij.plugin.language.LombokConfigFileType;
import jakarta.annotation.Nonnull;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@TopicImpl(ComponentScope.APPLICATION)
public class LombokConfigChangeListener implements BulkFileListener {
  private static final AtomicLong CONFIG_CHANGE_COUNTER = new AtomicLong(1);
  public static final ModificationTracker CONFIG_CHANGE_TRACKER = CONFIG_CHANGE_COUNTER::get;

  @Override
  public void before(@Nonnull List<? extends VFileEvent> events) {
    for (VFileEvent event : events) {
      VirtualFile eventFile = event.getFile();
      if (null != eventFile) {
        final CharSequence nameSequence = eventFile.getNameSequence();
        if (StringUtil.endsWith(nameSequence, "lombok.config")) {
          final FileType fileType = FileTypeRegistry.getInstance().getFileTypeByFileName(nameSequence);

          if (LombokConfigFileType.INSTANCE.equals(fileType)) {
            CONFIG_CHANGE_COUNTER.incrementAndGet();
            break;
          }
        }
      }
    }
  }
}
