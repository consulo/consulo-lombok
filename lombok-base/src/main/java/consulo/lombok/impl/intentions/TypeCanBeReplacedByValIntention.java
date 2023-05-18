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

package consulo.lombok.impl.intentions;

import com.intellij.java.language.psi.*;
import com.intellij.java.language.psi.codeStyle.JavaCodeStyleManager;
import consulo.annotation.component.ExtensionImpl;
import consulo.codeEditor.Editor;
import consulo.language.ast.ASTNode;
import consulo.language.codeStyle.CodeStyleManager;
import consulo.language.editor.intention.IntentionMetaData;
import consulo.language.editor.intention.PsiElementBaseIntentionAction;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.language.util.IncorrectOperationException;
import consulo.lombok.impl.LombokClassNames;
import consulo.lombok.impl.module.extension.LombokModuleExtension;
import consulo.lombok.impl.psi.impl.source.LombokValOwner;
import consulo.lombok.processors.util.LombokUtil;
import consulo.project.Project;

import jakarta.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 18.04.14
 */
@ExtensionImpl
@IntentionMetaData(ignoreId = "lombok.TypeCanBeReplacedByValIntention", fileExtensions = "java", categories = "Java/Lombok")
public class TypeCanBeReplacedByValIntention extends PsiElementBaseIntentionAction
{
	@Nonnull
	@Override
	public String getText()
	{
		return "Replace type by 'val'";
	}

	@Override
	public void invoke(@Nonnull Project project, Editor editor, @Nonnull PsiElement element) throws IncorrectOperationException
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
	public boolean isAvailable(@Nonnull Project project, Editor editor, @Nonnull PsiElement element)
	{
		if(!LombokUtil.isExtensionEnabled(element, LombokModuleExtension.class))
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
