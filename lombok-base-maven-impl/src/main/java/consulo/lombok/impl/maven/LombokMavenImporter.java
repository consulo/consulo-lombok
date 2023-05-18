/*
 * Copyright 2013 must-be.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package consulo.lombok.impl.maven;

import consulo.annotation.component.ExtensionImpl;
import consulo.maven.importing.MavenImporterFromDependency;
import consulo.module.Module;
import consulo.module.content.layer.ModifiableRootModel;
import consulo.module.extension.MutableModuleExtension;
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
@ExtensionImpl
public class LombokMavenImporter extends MavenImporterFromDependency
{
	public LombokMavenImporter()
	{
		super("org.projectlombok", "lombok");
	}

	@Override
	public void preProcess(Module module,
						   MavenProject mavenProject,
						   MavenProjectChanges mavenProjectChanges,
						   MavenModifiableModelsProvider mavenModifiableModelsProvider)
	{

	}

	@Override
	public void process(MavenModifiableModelsProvider mavenModifiableModelsProvider,
						Module module,
						MavenRootModelAdapter mavenRootModelAdapter,
						MavenProjectsTree mavenProjectsTree,
						MavenProject mavenProject,
						MavenProjectChanges mavenProjectChanges,
						Map<MavenProject, String> mavenProjectStringMap,
						List<MavenProjectsProcessorTask> mavenProjectsProcessorTasks)
	{
		final ModifiableRootModel rootModel = mavenModifiableModelsProvider.getRootModel(module);

		final MutableModuleExtension extensionWithoutCheck = rootModel.getExtensionWithoutCheck("lombok");

		extensionWithoutCheck.setEnabled(true);
	}
}
