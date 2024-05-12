package net.irisshaders.iris.mixin.texture;

import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.SpriteContents.AnimatedTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SpriteContents.Ticker.class)
public interface SpriteContentsTickerAccessor {
	@Accessor("frame")
	int getFrame();

	@Accessor("frame")
	void setFrame(int frame);

	@Accessor("subFrame")
	int getSubFrame();

	@Accessor("subFrame")
	void setSubFrame(int subFrame);

	@Accessor("animationInfo")
	AnimatedTexture getAnimationInfo();
}
