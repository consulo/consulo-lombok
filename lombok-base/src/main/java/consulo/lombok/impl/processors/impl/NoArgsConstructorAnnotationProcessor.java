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

import com.intellij.java.language.psi.PsiField;
import consulo.annotation.component.ExtensionImpl;
import jakarta.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 22:10/30.03.13
 */
@ExtensionImpl
public class NoArgsConstructorAnnotationProcessor extends NArgsConstructorAnnotationProcessor
{
	public NoArgsConstructorAnnotationProcessor()
	{
		super("lombok.NoArgsConstructor");
	}

	@Override
	protected boolean isFieldIsApplicable(@Nonnull PsiField psiField)
	{
		return false;
	}
}
