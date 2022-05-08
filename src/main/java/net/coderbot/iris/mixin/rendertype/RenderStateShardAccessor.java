package net.coderbot.iris.mixin.rendertype;

import net.minecraft.client.renderer.RenderStateShard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RenderStateShard.class)
public interface RenderStateShardAccessor {
	@Accessor("name")
	String getName();

	@Accessor("TRANSLUCENT_TRANSPARENCY")
	static RenderStateShard.TransparencyStateShard getTranslucentTransparency() {
		return null;
	}
}
