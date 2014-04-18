/*
 * Copyright 2013-2014 must-be.org
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

package org.consulo.lombok.intentions;

import org.consulo.lombok.LombokClassNames;
import org.consulo.lombok.processors.util.LombokUtil;
import org.consulo.lombok.psi.impl.source.LombokValOwner;
import org.jetbrains.annotations.NotNull;
import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;

/**
 * @author VISTALL
 * @since 18.04.14
 */
public class TypeCanBeReplacedByValIntention extends PsiElementBaseIntentionAction
{
	@NotNull
	@Override
	public String getText()
	{
		return "Replace type by 'val'";
	}

	@NotNull
	@Override
	public String getFamilyName()
	{
		return "Lombok";
	}

	@Override
	public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement element) throws IncorrectOperationException
	{
		PsiVariable variable = PsiTreeUtil.getParentOfType(element, PsiVariable.class);
		if(variable == null)
		{
			return;
		}

		PsiFile containingFile = variable.getContainingFile();
		if(!(containingFile instanceof PsiJavaFile))
		{
			return;
		}

		PsiTypeElement typeElement = variable.getTypeElement();
		if(typeElement == null)
		{
			return;
		}

		PsiClass valClass = JavaPsiFacade.getInstance(project).findClass(LombokClassNames.LOMBOK_VAL, variable.getResolveScope());

		if(valClass != null)
		{
			JavaCodeStyleManager.getInstance(project).addImport((PsiJavaFile) containingFile, valClass);
		}

		PsiTypeElement valTypeElement = JavaPsiFacade.getElementFactory(variable.getProject()).createTypeElementFromText("val", variable);


		PsiModifierList modifierList = variable.getModifierList();
		if(modifierList != null)
		{
			ASTNode childByType = modifierList.getNode().findChildByType(JavaTokenType.FINAL_KEYWORD);
			if(childByType != null)
			{
				childByType.getPsi().delete();
			}
		}

		typeElement.replace(valTypeElement);

		CodeStyleManager.getInstance(project).reformat(variable);
	}

	@Override
	public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement element)
	{
		if(!LombokUtil.isExtensionEnabled(element))
		{
			return false;
		}

		PsiVariable variable = PsiTreeUtil.getParentOfType(element, PsiVariable.class);
		if(variable instanceof LombokValOwner)
		{
			PsiType rightTypeIfCan = ((LombokValOwner) variable).findRightTypeIfCan();
			if(rightTypeIfCan == null)
			{
				final PsiTypeElement typeElement = variable.getTypeElement();
				if(typeElement == null)
				{
					return false;
				}

				PsiClass valClass = JavaPsiFacade.getInstance(project).findClass(LombokClassNames.LOMBOK_VAL, variable.getResolveScope());

				return valClass != null;
			}
		}
		return false;
	}

	@Override
	public boolean startInWriteAction()
	{
		return true;
	}
}
