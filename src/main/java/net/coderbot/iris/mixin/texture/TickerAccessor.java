package net.coderbot.iris.mixin.texture;

import net.minecraft.client.renderer.texture.SpriteContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SpriteContents.Ticker.class)
public interface TickerAccessor {
	@Accessor
	int getFrame();

	@Accessor
	int getSubFrame();


	@Accessor
	void setFrame(int frame);

	@Accessor
	void setSubFrame(int frame);
}
