package consulo.lombok.impl.psi;

import consulo.annotation.component.ExtensionImpl;
import consulo.lombok.impl.module.extension.LombokModuleExtension;
import consulo.lombok.psi.augment.LombokPsiAugmentProvider;
import consulo.module.extension.ModuleExtension;

import jakarta.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 2018-11-24
 */
@ExtensionImpl
public class LombokBasePsiAugmentProvider extends LombokPsiAugmentProvider
{
	@Nonnull
	@Override
	protected Class<? extends ModuleExtension> getModuleExtensionClass()
	{
		return LombokModuleExtension.class;
	}
}
