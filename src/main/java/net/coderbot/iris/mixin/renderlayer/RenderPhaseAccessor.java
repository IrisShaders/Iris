package net.coderbot.iris.mixin.renderlayer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.render.RenderPhase;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
@Mixin(RenderPhase.class)
public interface RenderPhaseAccessor {
	@Accessor("TRANSLUCENT_TRANSPARENCY")
	static RenderPhase.Transparency getTranslucentTransparency() {
		return null;
	}

	@Accessor("name")
	String getName();
}
