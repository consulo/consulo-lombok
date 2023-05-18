package consulo.lombok.pg.module.extension;

import consulo.annotation.component.ExtensionImpl;
import consulo.localize.LocalizeValue;
import consulo.lombok.pg.icon.LombokPgIconGroup;
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
public class LombokPgModuleExtensionProvider implements ModuleExtensionProvider<LombokPgModuleExtension>
{
	@Nonnull
	@Override
	public String getId()
	{
		return "lombok-pg";
	}

	@Nullable
	@Override
	public String getParentId()
	{
		return "lombok";
	}

	@Nonnull
	@Override
	public LocalizeValue getName()
	{
		return LocalizeValue.localizeTODO("Lombok-pg");
	}

	@Nonnull
	@Override
	public Image getIcon()
	{
		return LombokPgIconGroup.lombok();
	}

	@Nonnull
	@Override
	public ModuleExtension<LombokPgModuleExtension> createImmutableExtension(@Nonnull ModuleRootLayer moduleRootLayer)
	{
		return new LombokPgModuleExtension(getId(), moduleRootLayer);
	}

	@Nonnull
	@Override
	public MutableModuleExtension<LombokPgModuleExtension> createMutableExtension(@Nonnull ModuleRootLayer moduleRootLayer)
	{
		return new LombokPgMutableModuleExtension(getId(), moduleRootLayer);
	}
}
