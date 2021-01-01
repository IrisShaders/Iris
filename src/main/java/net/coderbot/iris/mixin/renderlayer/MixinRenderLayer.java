package net.coderbot.iris.mixin.renderlayer;

import java.util.Optional;

import net.coderbot.iris.layer.GbufferProgram;
import net.coderbot.iris.layer.ProgramRenderLayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.minecraft.client.render.RenderLayer;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
@Mixin(RenderLayer.class)
public class MixinRenderLayer implements ProgramRenderLayer {
	private static final String BUILD = "net/minecraft/client/render/RenderLayer$MultiPhaseParameters$Builder.build (Z)Lnet/minecraft/client/render/RenderLayer$MultiPhaseParameters;";

	@Override
	public Optional<GbufferProgram> getProgram() {
		// By default, don't use shaders to render content
		return Optional.empty();
	}

	@Inject(method = "<clinit>()V", at = @At(value = "INVOKE", target = BUILD), slice = @Slice(
		from = @At(value = "CONSTANT", args = "stringValue=solid"),
		to = @At(value = "FIELD", target = "net/minecraft/client/render/RenderLayer.CUTOUT:Lnet/minecraft/client/render/RenderLayer;")
	), locals = LocalCapture.PRINT)
	private static void addSolidShaders(CallbackInfo callback) {

	}

	static {
		RenderLayer.getSolid();
	}
}
