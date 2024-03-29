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
import com.intellij.java.language.psi.PsiSubstitutor;
import com.intellij.java.language.psi.PsiType;
import com.intellij.java.language.psi.PsiTypeParameter;
import com.intellij.java.language.psi.util.MethodSignature;
import com.intellij.java.language.psi.util.MethodSignatureUtil;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.psi.PsiElement;
import jakarta.annotation.Nonnull;
import jakarta.inject.Inject;

import java.util.List;
import java.util.Set;

/**
 * @author VISTALL
 * @since 21:03/30.03.13
 */
@ExtensionImpl
public class EqualsAndHashCodeAnnotationProcessor extends MethodCreatorByAnnotationProcessor
{
	@Inject
	public EqualsAndHashCodeAnnotationProcessor()
	{
		this("lombok.EqualsAndHashCode");
	}

	public EqualsAndHashCodeAnnotationProcessor(String annotationClass)
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

	@Nonnull
	@Override
	public MethodSignature[] getMethodSignatures(PsiClass psiClass)
	{
		return new MethodSignature[]{
				MethodSignatureUtil.createMethodSignature("hashCode", PsiType.EMPTY_ARRAY, PsiTypeParameter.EMPTY_ARRAY, PsiSubstitutor.EMPTY, false),
				MethodSignatureUtil.createMethodSignature("equals", new PsiType[]{PsiType.getJavaLangObject(psiClass.getManager(), psiClass.getResolveScope())}, PsiTypeParameter.EMPTY_ARRAY,
						PsiSubstitutor.EMPTY, false)
		};
	}

	@Nonnull
	@Override
	public PsiType[] getReturnTypes(PsiClass psiClass)
	{
		return new PsiType[]{
				PsiType.INT,
				PsiType.BOOLEAN
		};
	}
}
