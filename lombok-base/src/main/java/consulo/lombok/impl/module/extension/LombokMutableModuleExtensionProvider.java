package consulo.lombok.impl.module.extension;

import consulo.annotation.component.ExtensionImpl;
import consulo.localize.LocalizeValue;
import consulo.lombok.impl.icon.LombokIconGroup;
import consulo.module.content.layer.ModuleExtensionProvider;
import consulo.module.content.layer.ModuleRootLayer;
import consulo.module.extension.ModuleExtension;
import consulo.module.extension.MutableModuleExtension;
import consulo.ui.image.Image;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * @author VISTALL
 * @since 18/05/2023
 */
@ExtensionImpl
public class LombokMutableModuleExtensionProvider implements ModuleExtensionProvider<LombokModuleExtension>
{
	@Nonnull
	@Override
	public String getId()
	{
		return "lombok";
	}

	@Nullable
	@Override
	public String getParentId()
	{
		return "java";
	}

	@Nonnull
	@Override
	public LocalizeValue getName()
	{
		return LocalizeValue.localizeTODO("Lombok");
	}

	@Nonnull
	@Override
	public Image getIcon()
	{
		return LombokIconGroup.lombok();
	}

	@Nonnull
	@Override
	public ModuleExtension<LombokModuleExtension> createImmutableExtension(@Nonnull ModuleRootLayer moduleRootLayer)
	{
		return new LombokModuleExtension(getId(), moduleRootLayer);
	}

	@Nonnull
	@Override
	public MutableModuleExtension<LombokModuleExtension> createMutableExtension(@Nonnull ModuleRootLayer moduleRootLayer)
	{
		return new LombokMutableModuleExtension(getId(), moduleRootLayer);
	}
}
