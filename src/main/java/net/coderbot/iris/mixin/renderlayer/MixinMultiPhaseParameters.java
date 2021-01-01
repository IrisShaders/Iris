package net.coderbot.iris.mixin.renderlayer;

import java.util.Objects;

import net.coderbot.iris.layer.GbufferProgram;
import net.coderbot.iris.layer.MultiPhaseParametersExtension;
import net.coderbot.iris.layer.UseProgramRenderPhase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import net.minecraft.client.render.RenderLayer;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
@Mixin(RenderLayer.MultiPhaseParameters.class)
public class MixinMultiPhaseParameters implements MultiPhaseParametersExtension {
	@Unique
	private UseProgramRenderPhase program;

	@Override
	public void useProgram(GbufferProgram program) {
		if (this.program != null) {
			throw new IllegalStateException("Tried to add more than one iris:use_program render phase!");
		}

		this.program = new UseProgramRenderPhase(Objects.requireNonNull(program));
	}
}
