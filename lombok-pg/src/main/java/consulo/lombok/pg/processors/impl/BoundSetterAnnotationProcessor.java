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
package consulo.lombok.pg.processors.impl;

import java.util.List;

import javax.annotation.Nonnull;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifier;
import com.intellij.psi.PsiType;
import com.intellij.psi.impl.light.LightMethodBuilder;
import consulo.lombok.pg.module.extension.LombokPgModuleExtension;
import consulo.lombok.pg.processors.LombokPgFieldProcessor;
import consulo.lombok.processors.impl.SetterAnnotationProcessorBase;
import consulo.module.extension.ModuleExtension;

/**
 * @author VISTALL
 * @since 12:39/31.03.13
 */
public class BoundSetterAnnotationProcessor extends LombokPgFieldProcessor
{
	private static final String[] ourListenerNames = {
			"addPropertyChangeListener",
			"removePropertyChangeListener"
	};
	private static final String JAVA_BEAN_PROPERTY_CHANGE_LISTENER = "java.beans.PropertyChangeListener";

	private SetterAnnotationProcessorBase mySetterAnnotationProcessor;

	public BoundSetterAnnotationProcessor(String annotationClass)
	{
		super(annotationClass);
		mySetterAnnotationProcessor = new SetterAnnotationProcessorBase(annotationClass)
		{
			@Override
			public Class<? extends ModuleExtension> getModuleExtensionClass()
			{
				return LombokPgModuleExtension.class;
			}
		};
	}

	@Override
	public void processElement(@Nonnull PsiClass parent, @Nonnull PsiField psiField, @Nonnull List<PsiElement> result)
	{
		final PsiAnnotation affectedAnnotation = getAffectedAnnotation(psiField);

		for(String name : ourListenerNames)
		{
			LightMethodBuilder builder = new LightMethodBuilder(parent.getManager(), parent.getLanguage(), name);
			builder.addModifier(PsiModifier.PUBLIC);
			if(psiField.hasModifierProperty(PsiModifier.STATIC))
			{
				builder.addModifier(PsiModifier.STATIC);
			}
			builder.setMethodReturnType(PsiType.VOID);
			builder.setNavigationElement(affectedAnnotation);
			builder.setContainingClass(parent);
			builder.addParameter("listener", JavaPsiFacade.getElementFactory(parent.getProject()).createTypeByFQClassName(JAVA_BEAN_PROPERTY_CHANGE_LISTENER));

			result.add(builder);
		}

		mySetterAnnotationProcessor.processElement(parent, psiField, result);
	}

	@Nonnull
	@Override
	public Class<? extends PsiElement> getCollectorPsiElementClass()
	{
		return PsiMethod.class;
	}
}
