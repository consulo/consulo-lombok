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
package consulo.lombok.psi.augment;

import com.intellij.java.language.psi.PsiClass;
import com.intellij.java.language.psi.augment.PsiAugmentProvider;
import consulo.annotation.access.RequiredReadAction;
import consulo.component.extension.ExtensionPoint;
import consulo.language.psi.PsiElement;
import consulo.lombok.processors.LombokAnnotationOwnerProcessor;
import consulo.lombok.processors.LombokProcessor;
import consulo.lombok.processors.util.LombokUtil;
import consulo.module.extension.ModuleExtension;
import jakarta.annotation.Nonnull;

import java.util.*;

/**
 * @author VISTALL
 * @since 18:56/29.03.13
 */
public abstract class LombokPsiAugmentProvider extends PsiAugmentProvider
{
	@Nonnull
	@Override
	@RequiredReadAction
	public <Psi extends PsiElement> List<Psi> getAugments(@Nonnull PsiElement element, @Nonnull Class<Psi> type)
	{
		if(!LombokUtil.isExtensionEnabled(element, getModuleExtensionClass()))
		{
			return Collections.emptyList();
		}

		List<Psi> list = new ArrayList<>();

		Set<String> processedAnnotations = new HashSet<>();

		ExtensionPoint<LombokProcessor> point = element.getProject().getExtensionPoint(LombokProcessor.class);
		point.forEachExtensionSafe(processor ->
		{
			if(processor.getModuleExtensionClass() != getModuleExtensionClass())
			{
				return;
			}

			if(processor.getCollectorPsiElementClass() == type)
			{
				boolean processed = processor.process((PsiClass) element, (List<PsiElement>) list, processedAnnotations);

				if(processed && processor instanceof LombokAnnotationOwnerProcessor annotationOwnerProcessor)
				{
					processedAnnotations.add(annotationOwnerProcessor.getAnnotationClass());
				}
			}
		});
		return list;
	}

	@Nonnull
	protected abstract Class<? extends ModuleExtension> getModuleExtensionClass();
}
