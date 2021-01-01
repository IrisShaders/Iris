package net.coderbot.iris.mixin.renderlayer;

import net.coderbot.iris.layer.GbufferProgram;
import net.coderbot.iris.layer.ProgramHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import net.minecraft.client.render.RenderLayer;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
@Mixin(RenderLayer.MultiPhaseParameters.Builder.class)
public class MixinMultiPhaseParametersBuilder implements ProgramHolder {
	@Unique
	private GbufferProgram program;

	@Override
	public void program(GbufferProgram program) {
		this.program = program;
	}
}
