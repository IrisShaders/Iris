package net.irisshaders.iris.platform;

import com.mojang.blaze3d.textures.TextureFormat;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.SemanticVersion;
import net.fabricmc.loader.api.VersionParsingException;
import net.irisshaders.iris.gl.texture.DepthBufferFormat;
import net.minecraft.client.KeyMapping;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.lwjgl.system.Configuration;

import java.nio.file.Path;
import java.text.ParseException;

public class IrisFabricHelpers implements IrisPlatformHelpers {
	static {
		if (System.getProperty("user.home").contains("ims") && FabricLoader.getInstance().isDevelopmentEnvironment()) {
			//Configuration.GLFW_LIBRARY_NAME.set("/usr/lib/libglfw.so");
		}
	}
	@Override
	public boolean isModLoaded(String modId) {
		return FabricLoader.getInstance().isModLoaded(modId);
	}

	@Override
	public String getVersion() {
		return FabricLoader.getInstance().getModContainer("iris").get().getMetadata().getVersion().getFriendlyString();
	}

	@Override
	public boolean isDevelopmentEnvironment() {
		return FabricLoader.getInstance().isDevelopmentEnvironment();
	}

	@Override
	public Path getGameDir() {
		return FabricLoader.getInstance().getGameDir();
	}

	@Override
	public Path getConfigDir() {
		return FabricLoader.getInstance().getConfigDir();
	}

	@Override
	public int compareVersions(String currentVersion, String semanticVersion) throws Exception {
		try {
			return SemanticVersion.parse(currentVersion).compareTo(SemanticVersion.parse(semanticVersion));
		} catch (VersionParsingException e) {
			throw new Exception(e);
		}
	}

	@Override
	public KeyMapping registerKeyBinding(KeyMapping keyMapping) {
		return KeyBindingHelper.registerKeyBinding(keyMapping);
	}

	@Override
	public boolean useELS() {
		return false;
	}

	@Override
	public BlockState getBlockAppearance(BlockAndTintGetter level, BlockState state, Direction cullFace, BlockPos pos) {
		return state;
	}

	@Override
	public TextureFormat mojangDepthFormat(DepthBufferFormat depthFormat) {
		return switch (depthFormat) {
			case DEPTH -> TextureFormat.DEPTH32;
			case DEPTH16 -> null;
			case DEPTH24 -> null;
			case DEPTH32 -> TextureFormat.DEPTH32;
			case DEPTH32F -> null;
			case DEPTH_STENCIL -> null;
			case DEPTH24_STENCIL8 -> null;
			case DEPTH32F_STENCIL8 -> null;
		};
	}
}
