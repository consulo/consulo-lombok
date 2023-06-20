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

import com.intellij.java.language.psi.PsiClass;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.psi.PsiElement;
import consulo.lombok.impl.module.extension.LombokModuleExtension;
import consulo.lombok.processors.impl.SetterAnnotationProcessorBase;
import consulo.module.extension.ModuleExtension;
import jakarta.annotation.Nonnull;
import jakarta.inject.Inject;

import java.util.List;
import java.util.Set;

/**
 * @author VISTALL
 * @since 14:58/30.03.13
 */
@ExtensionImpl
public class SetterFieldAnnotationProcessor extends SetterAnnotationProcessorBase
{
	public static final String ANNOTATION_CLASS = "lombok.Setter";

	@Inject
	public SetterFieldAnnotationProcessor()
	{
		this(ANNOTATION_CLASS);
	}

	public SetterFieldAnnotationProcessor(String annotationClass)
	{
		super(annotationClass);
	}

	@Override
	public boolean process(@Nonnull PsiClass element, @Nonnull List<PsiElement> result, Set<String> processedAnnotations)
	{
		if(processedAnnotations.contains(DataAnnotationProcessor.ANNOTATION_CLASS))
		{
			return false;
		}
		return super.process(element, result, processedAnnotations);
	}

	@Override
	public Class<? extends ModuleExtension> getModuleExtensionClass()
	{
		return LombokModuleExtension.class;
	}
}
