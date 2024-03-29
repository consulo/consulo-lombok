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
package consulo.lombok.processors.util;

import com.intellij.java.language.impl.psi.impl.light.LightMethodBuilder;
import com.intellij.java.language.psi.*;
import consulo.annotation.access.RequiredReadAction;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiReference;
import consulo.module.Module;
import consulo.module.extension.ModuleExtension;

import jakarta.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 19:45/29.03.13
 */
public class LombokUtil
{
	@RequiredReadAction
	public static boolean isExtensionEnabled(@Nonnull PsiElement element, @Nonnull Class<? extends ModuleExtension> extensionClass)
	{
		Module module = element.getModule();
		if(module == null)
		{
			return false;
		}
		return module.getExtension(extensionClass) != null;
	}

	public static void copyAccessModifierFromOriginal(PsiModifierListOwner from, LightMethodBuilder to)
	{
		if(from.hasModifierProperty(PsiModifier.PRIVATE))
		{
			to.addModifier(PsiModifier.PRIVATE);
		}
		else if(from.hasModifierProperty(PsiModifier.PUBLIC))
		{
			to.addModifier(PsiModifier.PUBLIC);
		}
		else if(from.hasModifierProperty(PsiModifier.PROTECTED))
		{
			to.addModifier(PsiModifier.PROTECTED);
		}
	}

	public static void setAccessModifierFromAnnotation(@Nonnull PsiAnnotation annotation, LightMethodBuilder to, String methodName)
	{
		String modifier = getModifierFromAnnotation(annotation, methodName);
		to.addModifier(modifier);
	}

	@Nonnull
	public static String getModifierFromAnnotation(@Nonnull PsiAnnotation annotation, String methodName)
	{
		final PsiAnnotationMemberValue attributeValue = annotation.findAttributeValue(methodName);
		if(attributeValue instanceof PsiReference)
		{
			final PsiElement resolve = ((PsiReference) attributeValue).resolve();
			if(resolve instanceof PsiEnumConstant)
			{
				final String name = ((PsiEnumConstant) resolve).getName();

				if(name.equals("PUBLIC"))
				{
					return PsiModifier.PUBLIC;
				}
				else if(name.equals("PRIVATE"))
				{
					return PsiModifier.PRIVATE;
				}
				else if(name.equals("PROTECTED"))
				{
					return PsiModifier.PROTECTED;
				}
				else if(name.equals("PACKAGE"))
				{
					return PsiModifier.PACKAGE_LOCAL;
				}

				// FIXME [VISTALL] MODULE & NONE modifier
			}
		}
		return PsiModifier.PUBLIC;
	}
}
