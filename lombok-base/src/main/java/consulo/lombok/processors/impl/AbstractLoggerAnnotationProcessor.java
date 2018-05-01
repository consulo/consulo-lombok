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

import org.jetbrains.annotations.NotNull;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiModifier;
import com.intellij.psi.impl.light.LightFieldBuilder;
import consulo.lombok.module.extension.LombokModuleExtension;
import consulo.lombok.processors.LombokSelfClassProcessor;
import consulo.lombok.processors.util.LombokClassUtil;
import consulo.module.extension.ModuleExtension;

/**
 * @author VISTALL
 * @since 15:10/30.03.13
 */
public abstract class AbstractLoggerAnnotationProcessor extends LombokSelfClassProcessor
{
	protected AbstractLoggerAnnotationProcessor(String annotationClass)
	{
		super(annotationClass);
	}

	public abstract String getLoggerClass();

	@Override
	public Class<? extends ModuleExtension> getModuleExtensionClass()
	{
		return LombokModuleExtension.class;
	}

	@Override
	public void processElement(@NotNull final PsiClass parent, @NotNull PsiClass psiClass, @NotNull List<PsiElement> result)
	{
		final String fieldName = getFieldName();
		for(PsiField field : LombokClassUtil.getOwnFields(psiClass))
		{
			if(fieldName.equals(field.getName()))
			{
				return;
			}
		}

		PsiAnnotation annotation = getAffectedAnnotation(psiClass);

		LightFieldBuilder builder = new LightFieldBuilder(fieldName, getLoggerClass(), annotation);
		builder.setContainingClass(parent);
		//builder.getModifierList().addAnnotation("org.jetbrains.annotations.NotNull"); no annotation support
		builder.setModifiers(PsiModifier.PRIVATE, PsiModifier.FINAL, PsiModifier.STATIC);

		result.add(builder);
	}

	@NotNull
	protected String getFieldName()
	{
		return "log";
	}

	@NotNull
	@Override
	public Class<? extends PsiElement> getCollectorPsiElementClass()
	{
		return PsiField.class;
	}
}
