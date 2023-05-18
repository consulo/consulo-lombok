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
package consulo.lombok.impl.psi.impl.source;

import com.intellij.java.language.codeInsight.daemon.impl.analysis.JavaGenericsUtil;
import com.intellij.java.language.impl.psi.impl.source.PsiParameterImpl;
import com.intellij.java.language.psi.*;
import com.intellij.java.language.psi.util.PsiTypesUtil;
import consulo.application.util.RecursionManager;
import consulo.language.ast.ASTNode;
import consulo.language.psi.PsiElement;
import consulo.lombok.impl.LombokClassNames;
import consulo.lombok.impl.module.extension.LombokModuleExtension;
import consulo.lombok.processors.util.LombokUtil;
import consulo.util.collection.ArrayUtil;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.function.Supplier;

/**
 * @author VISTALL
 * @since 17:23/30.03.13
 */
public class LombokPsiParameterImpl extends PsiParameterImpl implements LombokValOwner
{
	private static final String[] ourAdditionalModifiers = new String[]{PsiModifier.FINAL};

	public LombokPsiParameterImpl(@Nonnull ASTNode node)
	{
		super(node);
	}

	@Nonnull
	@Override
	public PsiType getType()
	{
		PsiType type = findRightTypeIfCan();
		return type == null ? super.getType() : type;
	}

	@Override
	@Nullable
	public PsiType findRightTypeIfCan()
	{
		return RecursionManager.doPreventingRecursion(this, false, new Supplier<PsiType>()
		{
			@Nullable
			@Override
			public PsiType get()
			{
				if(!LombokUtil.isExtensionEnabled(LombokPsiParameterImpl.this, LombokModuleExtension.class))
				{
					return null;
				}

				final PsiElement parent = getParent();

				if(parent instanceof PsiForeachStatement)
				{
					final PsiClass resolve = PsiTypesUtil.getPsiClass(LombokPsiParameterImpl.super.getType());
					if(resolve == null || !LombokClassNames.LOMBOK_VAL.equals(resolve.getQualifiedName()))
					{
						return null;
					}
					final PsiExpression expression = ((PsiForeachStatement) parent).getIteratedValue();
					if(expression == null)
					{
						return null;
					}
					final PsiType collectionItemType = JavaGenericsUtil.getCollectionItemType(expression);
					return collectionItemType == null ? null : collectionItemType;
				}

				return null;
			}
		});
	}

	@Override
	public String[] getAdditionalModifiers()
	{
		return findRightTypeIfCan() == null ? ArrayUtil.EMPTY_STRING_ARRAY : ourAdditionalModifiers;
	}
}
