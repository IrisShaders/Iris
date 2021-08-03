package net.coderbot.iris.mixin.renderlayer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.RenderStateShard;

@Environment(EnvType.CLIENT)
@Mixin(RenderStateShard.class)
public interface RenderPhaseAccessor {
	@Accessor("name")
	String getName();

	@Accessor("TRANSLUCENT_TRANSPARENCY")
	static RenderStateShard.TransparencyStateShard getTranslucentTransparency() {
		return null;
	}
}
