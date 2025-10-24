package de.plushnikov.intellij.plugin.util;

import com.intellij.java.language.psi.JavaPsiFacade;
import com.intellij.java.language.psi.PsiJavaPackage;
import consulo.annotation.access.RequiredReadAction;
import consulo.application.Application;
import consulo.application.ReadAction;
import consulo.application.util.CachedValueProvider;
import consulo.application.util.CachedValuesManager;
import consulo.component.ProcessCanceledException;
import consulo.language.psi.PsiDirectory;
import consulo.logging.Logger;
import consulo.module.content.ProjectRootManager;
import consulo.module.content.layer.orderEntry.OrderEntry;
import consulo.project.Project;
import consulo.util.lang.StringUtil;
import de.plushnikov.intellij.plugin.Version;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.List;

public final class LombokLibraryUtil {
    private static final Logger LOG = Logger.getInstance(LombokLibraryUtil.class);

    private static final String LOMBOK_PACKAGE = "lombok.experimental";

    @RequiredReadAction
    public static boolean hasLombokLibrary(@Nonnull Project project) {
        if (project.isDefault() || !project.isInitialized()) {
            return false;
        }
        Application.get().assertReadAccessAllowed();
        return CachedValuesManager.getManager(project).getCachedValue(
            project,
            () -> {
                PsiJavaPackage aPackage = JavaPsiFacade.getInstance(project).findPackage(LOMBOK_PACKAGE);
                return new CachedValueProvider.Result<>(aPackage, ProjectRootManager.getInstance(project));
            }
        ) != null;
    }

    @Nonnull
    public static String getLombokVersionCached(@Nonnull Project project) {
        return CachedValuesManager.getManager(project).getCachedValue(
            project,
            () -> {
                String lombokVersion = null;
                try {
                    lombokVersion = ReadAction.nonBlocking(() -> getLombokVersionInternal(project)).executeSynchronously();
                }
                catch (ProcessCanceledException e) {
                    throw e;
                }
                catch (Throwable e) {
                    LOG.error(e);
                }
                return new CachedValueProvider.Result<>(StringUtil.notNullize(lombokVersion), ProjectRootManager.getInstance(project));
            }
        );
    }

    @Nullable
    private static String getLombokVersionInternal(@Nonnull Project project) {
        PsiJavaPackage aPackage = JavaPsiFacade.getInstance(project).findPackage(LOMBOK_PACKAGE);
        if (aPackage != null) {
            PsiDirectory[] directories = aPackage.getDirectories();
            if (directories.length > 0) {
                List<OrderEntry> entries =
                    ProjectRootManager.getInstance(project).getFileIndex().getOrderEntriesForFile(directories[0].getVirtualFile());
                if (!entries.isEmpty()) {
                    return Version.parseLombokVersion(entries.get(0));
                }
            }
        }
        return null;
    }
}
