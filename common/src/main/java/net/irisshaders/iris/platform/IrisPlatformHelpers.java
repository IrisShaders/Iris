package net.irisshaders.iris.platform;

import com.mojang.blaze3d.textures.TextureFormat;
import net.irisshaders.iris.gl.texture.DepthBufferFormat;
import net.minecraft.client.KeyMapping;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

import java.nio.file.Path;
import java.util.ServiceLoader;

public interface IrisPlatformHelpers {
	IrisPlatformHelpers INSTANCE = ServiceLoader.load(IrisPlatformHelpers.class).findFirst().get();

	static IrisPlatformHelpers getInstance() {
		return INSTANCE;
	}

	boolean isModLoaded(String modId);

	String getVersion();

	boolean isDevelopmentEnvironment();

	Path getGameDir();

	Path getConfigDir();

	int compareVersions(String currentVersion, String semanticVersion) throws Exception;

	KeyMapping registerKeyBinding(KeyMapping keyMapping);

	boolean useELS();

    BlockState getBlockAppearance(BlockAndTintGetter level, BlockState state, Direction cullFace, BlockPos pos);

    TextureFormat mojangDepthFormat(DepthBufferFormat depthFormat);
}
