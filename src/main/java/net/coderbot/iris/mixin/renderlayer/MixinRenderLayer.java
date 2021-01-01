package net.coderbot.iris.mixin.renderlayer;

import java.util.Optional;

import net.coderbot.iris.layer.GbufferProgram;
import net.coderbot.iris.layer.ProgramRenderLayer;
import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.client.render.RenderLayer;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
@Mixin(RenderLayer.class)
public class MixinRenderLayer implements ProgramRenderLayer {
	@Override
	public Optional<GbufferProgram> getProgram() {
		// By default, don't use shaders to render content
		return Optional.empty();
	}
}
