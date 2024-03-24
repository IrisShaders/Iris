package net.irisshaders.iris.texture.mipmap;

import com.mojang.blaze3d.platform.NativeImage;
import org.jetbrains.annotations.Nullable;

public interface CustomMipmapGenerator {
	NativeImage[] generateMipLevels(NativeImage[] image, int mipLevel);

	interface Provider {
		@Nullable
		CustomMipmapGenerator getMipmapGenerator();
	}
}
