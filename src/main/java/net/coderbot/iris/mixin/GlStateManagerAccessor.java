package net.coderbot.iris.mixin;

import com.mojang.blaze3d.platform.GlStateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GlStateManager.class)
public interface GlStateManagerAccessor {
	@Accessor("activeTexture")
	static int getActiveTexture() { throw new UnsupportedOperationException("not accessed"); }

	@Accessor("BLEND")
	static GlStateManager.BlendState getBLEND() {
		throw new UnsupportedOperationException("Not accessed");
	}
}
