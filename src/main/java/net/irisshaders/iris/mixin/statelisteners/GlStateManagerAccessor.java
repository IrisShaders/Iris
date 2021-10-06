package net.irisshaders.iris.mixin.statelisteners;

import com.mojang.blaze3d.platform.GlStateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GlStateManager.class)
public interface GlStateManagerAccessor {
	@Accessor("FOG")
	static GlStateManager.FogState getFOG() {
		throw new AssertionError();
	}

	@Accessor("TEXTURES")
	static GlStateManager.TextureState[] getTEXTURES() {
		throw new AssertionError();
	}
}
