package net.coderbot.iris.mixin.renderlayer;

import net.coderbot.iris.layer.GbufferProgram;
import net.coderbot.iris.layer.MultiPhaseParametersExtension;
import net.coderbot.iris.layer.ProgramHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

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

	@Inject(method = "build(Lnet/minecraft/client/render/RenderLayer$OutlineMode;)Lnet/minecraft/client/render/RenderLayer$MultiPhaseParameters;", at = @At("RETURN"))
	private void iris$addProgramToBuiltParameters(@Coerce Object outlineMode, CallbackInfoReturnable<RenderLayer.MultiPhaseParameters> cir) {
		if (this.program == null) {
			return;
		}

		MultiPhaseParametersExtension extension = (MultiPhaseParametersExtension) (Object) cir.getReturnValue();

		extension.useProgram(this.program);
	}
}
