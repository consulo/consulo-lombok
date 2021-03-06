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

package consulo.lombok.codeInsight;

import javax.annotation.Nonnull;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.JavaElementVisitor;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElementVisitor;
import consulo.lombok.module.extension.LombokModuleExtension;
import consulo.lombok.processors.LombokProcessor;
import consulo.lombok.processors.LombokProcessorEP;
import consulo.lombok.processors.util.LombokUtil;

/**
 * @author VISTALL
 * @since 19:34/02.05.13
 */
public class LombokLocalInspection extends LocalInspectionTool
{
	@Nonnull
	@Override
	public PsiElementVisitor buildVisitor(@Nonnull final ProblemsHolder holder, boolean isOnTheFly)
	{
		if(!LombokUtil.isExtensionEnabled(holder.getFile(), LombokModuleExtension.class))
		{
			return new JavaElementVisitor()
			{
			};
		}
		return new JavaElementVisitor()
		{
			@Override
			public void visitClass(PsiClass aClass)
			{
				for(LombokProcessorEP ep : LombokProcessorEP.EP_NAME.getExtensions())
				{
					LombokProcessor instance = ep.getInstance();

					instance.collectInspections(aClass, holder);
				}
			}
		};
	}
}
