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

package org.consulo.lombok.pg.module.extension;

import javax.swing.JComponent;

import org.consulo.module.extension.MutableModuleExtension;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.openapi.roots.ModifiableRootModel;

/**
 * @author VISTALL
 * @since 25.05.13
 */
public class LombokPgMutableModuleExtension extends LombokPgModuleExtension implements MutableModuleExtension<LombokPgModuleExtension>
{
	public LombokPgMutableModuleExtension(@NotNull String id, @NotNull ModifiableRootModel module)
	{
		super(id, module);
	}

	@Nullable
	@Override
	public JComponent createConfigurablePanel(@Nullable Runnable runnable)
	{
		return null;
	}

	@Override
	public void setEnabled(boolean b)
	{
		myIsEnabled = b;
	}

	@Override
	public boolean isModified(@NotNull LombokPgModuleExtension extension)
	{
		return myIsEnabled != extension.isEnabled();
	}
}
