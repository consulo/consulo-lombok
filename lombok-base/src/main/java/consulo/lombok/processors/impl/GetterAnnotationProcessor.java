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

import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifier;
import com.intellij.psi.impl.light.LightMethodBuilder;
import com.intellij.psi.util.PropertyUtil;
import consulo.lombok.module.extension.LombokModuleExtension;
import consulo.lombok.processors.LombokFieldProcessor;
import consulo.lombok.processors.util.LombokUtil;
import consulo.module.extension.ModuleExtension;

/**
 * @author VISTALL
 * @since 18:49/29.03.13
 */
public class GetterAnnotationProcessor extends LombokFieldProcessor
{
	public GetterAnnotationProcessor(String annotationClass)
	{
		super(annotationClass);
	}

	@Override
	public Class<? extends ModuleExtension> getModuleExtensionClass()
	{
		return LombokModuleExtension.class;
	}

	@Override
	public void processElement(@Nonnull PsiClass parent, @Nonnull PsiField psiField, @Nonnull List<PsiElement> result)
	{
		LightMethodBuilder builder = new LightMethodBuilder(parent.getManager(), parent.getLanguage(), PropertyUtil.suggestGetterName(psiField));
		builder.setMethodReturnType(psiField.getType());
		builder.setContainingClass(parent);
		builder.setNavigationElement(psiField);

		if(psiField.hasModifierProperty(PsiModifier.STATIC))
		{
			builder.addModifier(PsiModifier.STATIC);
		}

		PsiAnnotation annotation = getAffectedAnnotation(psiField);

		LombokUtil.setAccessModifierFromAnnotation(annotation, builder, PsiAnnotation.DEFAULT_REFERENCED_METHOD_NAME);

		result.add(builder);
	}

	@Nonnull
	@Override
	public Class<? extends PsiElement> getCollectorPsiElementClass()
	{
		return PsiMethod.class;
	}
}
