package net.coderbot.iris.mixin;

import com.mojang.blaze3d.platform.GlStateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GlStateManager.class)
public interface GlStateManagerAccessor {
	@Accessor("activeTexture")
	static int getActiveTexture() {
		throw new AssertionError();
	}

	@Accessor("ALPHA_TEST")
	static GlStateManager.AlphaState getALPHA_TEST() {
		throw new UnsupportedOperationException("Not accessed");
	}

	@Accessor("BLEND")
	static GlStateManager.BlendState getBLEND() {
		throw new UnsupportedOperationException("Not accessed");
	}

	@Accessor("COLOR_MASK")
	static GlStateManager.ColorMask getCOLOR_MASK() {
		throw new UnsupportedOperationException("Not accessed");
	}

	@Accessor("DEPTH")
	static GlStateManager.DepthState getDEPTH() {
		throw new UnsupportedOperationException("Not accessed");
	}

	@Accessor("FOG")
	static GlStateManager.FogState getFOG() {
		throw new UnsupportedOperationException("Not accessed");
	}

	@Accessor("TEXTURES")
	static GlStateManager.TextureState[] getTEXTURES() {
		throw new UnsupportedOperationException("Not accessed");
	}
}
