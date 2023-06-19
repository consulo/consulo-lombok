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
import jakarta.annotation.Nonnull;
import jakarta.inject.Inject;

import java.util.List;
import java.util.Set;

/**
 * @author VISTALL
 * @since 18:20/31.03.13
 */
@ExtensionImpl
public class AllArgsConstructorAnnotationProcessor extends NArgsConstructorAnnotationProcessor
{
	@Inject
	public AllArgsConstructorAnnotationProcessor()
	{
		this("lombok.AllArgsConstructor");
	}

	public AllArgsConstructorAnnotationProcessor(String annotationClass)
	{
		super(annotationClass);
	}

	@Override
	public void process(@Nonnull PsiClass element, @Nonnull List<PsiElement> result, Set<String> processedAnnotations)
	{
		if(processedAnnotations.contains(DataAnnotationProcessor.ANNOTATION_CLASS))
		{
			return;
		}

		super.process(element, result, processedAnnotations);
	}
}
