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

import com.intellij.java.language.impl.psi.impl.light.LightMethodBuilder;
import com.intellij.java.language.psi.*;
import com.intellij.java.language.psi.util.PropertyUtil;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.psi.PsiElement;
import consulo.lombok.impl.module.extension.LombokModuleExtension;
import consulo.lombok.processors.LombokFieldProcessor;
import consulo.lombok.processors.util.LombokUtil;
import consulo.module.extension.ModuleExtension;
import jakarta.annotation.Nonnull;
import jakarta.inject.Inject;

import java.util.List;
import java.util.Set;

/**
 * @author VISTALL
 * @since 18:49/29.03.13
 */
@ExtensionImpl
public class GetterFieldAnnotationProcessor extends LombokFieldProcessor
{
	public static final String ANNOTATION_CLASS = "lombok.Getter";

	@Inject
	public GetterFieldAnnotationProcessor()
	{
		this(ANNOTATION_CLASS);
	}

	public GetterFieldAnnotationProcessor(String annotationClass)
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
	public Class<? extends ModuleExtension> getModuleExtensionClass()
	{
		return LombokModuleExtension.class;
	}

	@Override
	public void processElement(@Nonnull PsiClass parent, @Nonnull PsiField psiField, @Nonnull List<PsiElement> result)
	{
		PsiAnnotation annotation = getAffectedAnnotation(psiField);

		LightMethodBuilder builder = createGetter(parent, psiField, annotation);

		result.add(builder);
	}

	@Nonnull
	public static LightMethodBuilder createGetter(@Nonnull PsiClass parent, @Nonnull PsiField psiField, PsiAnnotation annotation)
	{
		LightMethodBuilder builder = new LightMethodBuilder(parent.getManager(), parent.getLanguage(), PropertyUtil.suggestGetterName(psiField));
		builder.setMethodReturnType(psiField.getType());
		builder.setContainingClass(parent);
		builder.setNavigationElement(psiField);

		if(psiField.hasModifierProperty(PsiModifier.STATIC))
		{
			builder.addModifier(PsiModifier.STATIC);
		}

		LombokUtil.setAccessModifierFromAnnotation(annotation, builder, PsiAnnotation.DEFAULT_REFERENCED_METHOD_NAME);
		return builder;
	}

	@Nonnull
	@Override
	public Class<? extends PsiElement> getCollectorPsiElementClass()
	{
		return PsiMethod.class;
	}
}
