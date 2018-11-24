package consulo.lombok.psi;

import javax.annotation.Nonnull;

import consulo.lombok.module.extension.LombokModuleExtension;
import consulo.lombok.psi.augment.LombokPsiAugmentProvider;
import consulo.module.extension.ModuleExtension;

/**
 * @author VISTALL
 * @since 2018-11-24
 */
public class LombokBasePsiAugmentProvider extends LombokPsiAugmentProvider
{
	@Nonnull
	@Override
	protected Class<? extends ModuleExtension> getModuleExtensionClass()
	{
		return LombokModuleExtension.class;
	}
}
