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
package consulo.lombok.psi.impl.source;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.intellij.codeInsight.daemon.impl.analysis.JavaGenericsUtil;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.NullableComputable;
import com.intellij.openapi.util.RecursionManager;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiExpression;
import com.intellij.psi.PsiForeachStatement;
import com.intellij.psi.PsiModifier;
import com.intellij.psi.PsiType;
import com.intellij.psi.impl.source.PsiParameterImpl;
import com.intellij.psi.util.PsiTypesUtil;
import com.intellij.util.ArrayUtil;
import consulo.lombok.LombokClassNames;
import consulo.lombok.module.extension.LombokModuleExtension;
import consulo.lombok.processors.util.LombokUtil;

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
		return RecursionManager.doPreventingRecursion(this, false, new NullableComputable<PsiType>()
		{
			@Nullable
			@Override
			public PsiType compute()
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
