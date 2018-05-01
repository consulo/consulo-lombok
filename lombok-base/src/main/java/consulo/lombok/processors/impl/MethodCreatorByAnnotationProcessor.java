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
package consulo.lombok.processors.impl;

import java.util.List;

import javax.annotation.Nonnull;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiSubstitutor;
import com.intellij.psi.PsiType;
import com.intellij.psi.impl.light.LightMethodBuilder;
import com.intellij.psi.util.MethodSignature;
import com.intellij.psi.util.MethodSignatureUtil;
import consulo.lombok.module.extension.LombokModuleExtension;
import consulo.lombok.processors.LombokSelfClassProcessor;
import consulo.lombok.processors.util.LombokClassUtil;
import consulo.module.extension.ModuleExtension;

/**
 * @author VISTALL
 * @since 21:03/30.03.13
 */
public abstract class MethodCreatorByAnnotationProcessor extends LombokSelfClassProcessor
{
	public MethodCreatorByAnnotationProcessor(String annotationClass)
	{
		super(annotationClass);
	}

	@Override
	public Class<? extends ModuleExtension> getModuleExtensionClass()
	{
		return LombokModuleExtension.class;
	}

	@Nonnull
	public abstract MethodSignature[] getMethodSignatures(PsiClass psiClass);

	@Nonnull
	public abstract PsiType[] getReturnTypes(PsiClass psiClass);

	@Override
	public void processElement(@Nonnull PsiClass parent, @Nonnull PsiClass psiClass, @Nonnull List<PsiElement> result)
	{
		MethodSignature[] methodSignatures = getMethodSignatures(psiClass);
		PsiType[] returnTypes = getReturnTypes(psiClass);

		for(int i = 0; i < methodSignatures.length; i++)
		{
			MethodSignature methodSignature = methodSignatures[i];
			PsiType returnType = returnTypes[i];
			if(!isCanBeApplied(psiClass, methodSignature))
			{
				continue;
			}

			LightMethodBuilder builder = new LightMethodBuilder(parent.getManager(), parent.getLanguage(), methodSignature.getName());
			int k = 0;
			for(PsiType psiType : methodSignature.getParameterTypes())
			{
				builder.addParameter("p" + k, psiType);
				k++;
			}
			builder.setMethodReturnType(returnType);
			builder.setNavigationElement(getAffectedAnnotation(psiClass));
			builder.setContainingClass(psiClass);
			result.add(builder);
		}
	}

	private boolean isCanBeApplied(PsiClass psiClass, MethodSignature methodSignature)
	{
		for(PsiMethod method : LombokClassUtil.getOwnMethods(psiClass))
		{
			if(MethodSignatureUtil.areSignaturesEqual(method.getSignature(PsiSubstitutor.EMPTY), methodSignature))
			{
				return false;
			}
		}
		return true;
	}

	@Nonnull
	@Override
	public Class<? extends PsiElement> getCollectorPsiElementClass()
	{
		return PsiMethod.class;
	}
}
