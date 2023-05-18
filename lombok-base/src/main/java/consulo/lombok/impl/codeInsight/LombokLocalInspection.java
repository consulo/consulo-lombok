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

package consulo.lombok.impl.codeInsight;

import com.intellij.java.language.JavaLanguage;
import com.intellij.java.language.psi.JavaElementVisitor;
import com.intellij.java.language.psi.PsiClass;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.Language;
import consulo.language.editor.inspection.LocalInspectionTool;
import consulo.language.editor.inspection.ProblemsHolder;
import consulo.language.editor.rawHighlight.HighlightDisplayLevel;
import consulo.language.psi.PsiElementVisitor;
import consulo.lombok.impl.module.extension.LombokModuleExtension;
import consulo.lombok.processors.LombokProcessor;
import consulo.lombok.processors.util.LombokUtil;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * @author VISTALL
 * @since 19:34/02.05.13
 */
@ExtensionImpl
public class LombokLocalInspection extends LocalInspectionTool
{
	@Nonnull
	@Override
	public String getDisplayName()
	{
		return "Lombok Errors";
	}

	@Nullable
	@Override
	public Language getLanguage()
	{
		return JavaLanguage.INSTANCE;
	}

	@Nonnull
	@Override
	public HighlightDisplayLevel getDefaultLevel()
	{
		return HighlightDisplayLevel.ERROR;
	}

	@Nonnull
	@Override
	public String getGroupDisplayName()
	{
		return "Lombok";
	}

	@Nonnull
	@Override
	public PsiElementVisitor buildVisitor(@Nonnull final ProblemsHolder holder, boolean isOnTheFly)
	{
		if(!LombokUtil.isExtensionEnabled(holder.getFile(), LombokModuleExtension.class))
		{
			return PsiElementVisitor.EMPTY_VISITOR;
		}
		return new JavaElementVisitor()
		{
			@Override
			public void visitClass(PsiClass aClass)
			{
				holder.getProject().getExtensionPoint(LombokProcessor.class).forEachExtensionSafe(lombokProcessor ->
				{
					lombokProcessor.collectInspections(aClass, holder);
				});
			}
		};
	}
}
