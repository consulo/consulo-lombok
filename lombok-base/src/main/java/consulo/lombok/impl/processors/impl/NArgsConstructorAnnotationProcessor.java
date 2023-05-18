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

import com.intellij.java.language.impl.psi.impl.light.LightMethod;
import com.intellij.java.language.impl.psi.impl.light.LightMethodBuilder;
import com.intellij.java.language.impl.psi.impl.light.LightTypeParameter;
import com.intellij.java.language.psi.*;
import consulo.language.psi.PsiElement;
import consulo.lombok.impl.module.extension.LombokModuleExtension;
import consulo.lombok.processors.LombokSelfClassProcessor;
import consulo.lombok.processors.util.LombokClassUtil;
import consulo.lombok.processors.util.LombokUtil;
import consulo.module.extension.ModuleExtension;
import consulo.util.collection.ContainerUtil;
import jakarta.annotation.Nonnull;

import java.util.List;

/**
 * @author VISTALL
 * @since 18:25/31.03.13
 */
public abstract class NArgsConstructorAnnotationProcessor extends LombokSelfClassProcessor
{
	public NArgsConstructorAnnotationProcessor(String annotationClass)
	{
		super(annotationClass);
	}

	@Override
	public Class<? extends ModuleExtension> getModuleExtensionClass()
	{
		return LombokModuleExtension.class;
	}

	@Override
	public void processElement(@Nonnull PsiClass parent, @Nonnull PsiClass psiClass, @Nonnull List<PsiElement> result)
	{
		final PsiAnnotation affectedAnnotation = getAffectedAnnotation(psiClass);

		StringBuilder builder = new StringBuilder();
		String modifier = LombokUtil.getModifierFromAnnotation(affectedAnnotation, "access");
		if(modifier == null)
		{
			modifier = PsiModifier.PUBLIC;
		}

		builder.append(modifier).append(" ").append(psiClass.getName()).append("(");
		List<PsiField> applicableFields = LombokClassUtil.getOwnFields(psiClass);
		applicableFields = ContainerUtil.filter(applicableFields, this::isFieldIsApplicable);

		for(int i = 0; i < applicableFields.size(); i++)
		{
			if(i != 0)
			{
				builder.append(", ");
			}
			PsiField field = applicableFields.get(i);
			builder.append(field.getType().getCanonicalText()).append(" ").append(field.getName());
		}
		builder.append(") {\n");

		for(PsiField psiField : applicableFields)
		{
			builder.append("this.").append(psiField.getName()).append(" = ").append(psiField.getName()).append(";\n");
		}
		builder.append("}");

		PsiMethod methodFromText = JavaPsiFacade.getElementFactory(psiClass.getProject()).createMethodFromText(builder.toString(), psiClass);

		LightMethod constructor = new LightMethod(psiClass.getManager(), methodFromText, psiClass);
		constructor.setNavigationElement(affectedAnnotation);

		result.add(constructor);

		createStaticConstructor(parent, psiClass, result, affectedAnnotation, applicableFields);
	}

	private void createStaticConstructor(PsiClass parent, PsiClass psiClass, List<PsiElement> result, PsiAnnotation affectedAnnotation, List<PsiField> applicableFields)
	{
		final PsiAnnotationMemberValue staticName = affectedAnnotation.findAttributeValue(getStaticConstructorAttributeName());
		if(staticName instanceof PsiLiteralExpression)
		{
			final Object value = ((PsiLiteralExpression) staticName).getValue();
			if(value instanceof String)
			{
				LightMethodBuilder methodBuilder = new LightMethodBuilder(psiClass.getManager(), parent.getLanguage(), (String) value);
				methodBuilder.setContainingClass(psiClass);
				methodBuilder.setNavigationElement(affectedAnnotation);
				methodBuilder.addModifiers(PsiModifier.STATIC, PsiModifier.PUBLIC);
				methodBuilder.setMethodReturnType(JavaPsiFacade.getElementFactory(psiClass.getProject()).createType(psiClass));
				for(PsiTypeParameter typeParameter : psiClass.getTypeParameters())
				{
					methodBuilder.addTypeParameter(new LightTypeParameter(typeParameter));
				}

				for(PsiField field : applicableFields)
				{
					methodBuilder.addParameter(field.getName(), field.getType());
				}

				result.add(methodBuilder);
			}
		}
	}

	protected boolean isFieldIsApplicable(@Nonnull PsiField psiField)
	{
		return !psiField.hasModifierProperty(PsiModifier.STATIC);
	}

	@Nonnull
	protected String getStaticConstructorAttributeName()
	{
		return "staticName";
	}

	@Nonnull
	@Override
	public Class<? extends PsiElement> getCollectorPsiElementClass()
	{
		return PsiMethod.class;
	}
}
