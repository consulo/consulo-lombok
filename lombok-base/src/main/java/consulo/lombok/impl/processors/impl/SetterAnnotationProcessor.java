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
package consulo.lombok.impl.processors.impl;

import consulo.annotation.component.ExtensionImpl;
import consulo.lombok.impl.module.extension.LombokModuleExtension;
import consulo.lombok.processors.impl.SetterAnnotationProcessorBase;
import consulo.module.extension.ModuleExtension;
import jakarta.inject.Inject;

/**
 * @author VISTALL
 * @since 14:58/30.03.13
 */
@ExtensionImpl
public class SetterAnnotationProcessor extends SetterAnnotationProcessorBase
{
	@Inject
	public SetterAnnotationProcessor()
	{
		this("lombok.Setter");
	}

	public SetterAnnotationProcessor(String annotationClass)
	{
		super(annotationClass);
	}

	@Override
	public Class<? extends ModuleExtension> getModuleExtensionClass()
	{
		return LombokModuleExtension.class;
	}
}
