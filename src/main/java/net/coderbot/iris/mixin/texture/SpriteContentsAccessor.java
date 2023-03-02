package net.coderbot.iris.mixin.texture;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.SpriteContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SpriteContents.class)
public interface SpriteContentsAccessor {
	@Accessor("byMipLevel")
	NativeImage[] getByMipLevel();

	@Accessor("animatedTexture")
	SpriteContents.AnimatedTexture getAnimatedTexture();
}
