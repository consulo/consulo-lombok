package org.consulo.lombok.maven;

import com.intellij.openapi.module.Module;
import org.consulo.lombok.module.extension.LombokModuleExtension;
import org.jetbrains.idea.maven.importing.MavenImporterFromDependency;
import org.jetbrains.idea.maven.importing.MavenModifiableModelsProvider;
import org.jetbrains.idea.maven.importing.MavenRootModelAdapter;
import org.jetbrains.idea.maven.project.MavenProject;
import org.jetbrains.idea.maven.project.MavenProjectChanges;
import org.jetbrains.idea.maven.project.MavenProjectsProcessorTask;
import org.jetbrains.idea.maven.project.MavenProjectsTree;

import java.util.List;
import java.util.Map;

/**
 * @author VISTALL
 * @since 12.07.13.
 */
public class LombokMavenImporter extends MavenImporterFromDependency {
  public LombokMavenImporter() {
    super("org.projectlombok", "lombok");
  }

  @Override
  public void preProcess(Module module,
                         MavenProject mavenProject,
                         MavenProjectChanges mavenProjectChanges,
                         MavenModifiableModelsProvider mavenModifiableModelsProvider) {

  }

  @Override
  public void process(MavenModifiableModelsProvider mavenModifiableModelsProvider,
                      Module module,
                      MavenRootModelAdapter mavenRootModelAdapter,
                      MavenProjectsTree mavenProjectsTree,
                      MavenProject mavenProject,
                      MavenProjectChanges mavenProjectChanges,
                      Map<MavenProject, String> mavenProjectStringMap,
                      List<MavenProjectsProcessorTask> mavenProjectsProcessorTasks) {
    enableModuleExtension(module, mavenModifiableModelsProvider, LombokModuleExtension.class);
  }
}
