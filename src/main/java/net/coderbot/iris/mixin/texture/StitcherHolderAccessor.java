package net.coderbot.iris.mixin.texture;

import net.minecraft.client.renderer.texture.Stitcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Stitcher.Holder.class)
public interface StitcherHolderAccessor {
	@Accessor
	int getWidth();

	@Accessor
	int getHeight();
}
