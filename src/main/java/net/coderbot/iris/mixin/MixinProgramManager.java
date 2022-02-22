package net.coderbot.iris.mixin;

import com.mojang.blaze3d.shaders.ProgramManager;
import com.mojang.blaze3d.shaders.Shader;
import net.coderbot.iris.pipeline.newshader.ExtendedShader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ProgramManager.class)
public class MixinProgramManager {
	@Inject(method = "releaseProgram", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;assertOnRenderThread()V"))
	private static void iris$releaseGeometry(Shader shader, CallbackInfo ci) {
		if (shader instanceof ExtendedShader && ((ExtendedShader) shader).getGeometry() != null) {
			((ExtendedShader) shader).getGeometry().close();
		}
	}
}
