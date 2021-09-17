package net.coderbot.iris.mixin;

import com.mojang.blaze3d.shaders.Uniform;
import net.coderbot.iris.pipeline.newshader.ExtendedShader;
import net.minecraft.client.renderer.ShaderInstance;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ShaderInstance.class)
public class MixinShaderInstance {
	@Redirect(method = {"apply", "updateLocations"}, at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/shaders/Uniform;glGetUniformLocation(ILjava/lang/CharSequence;)I"))
	private int iris$redirectGetUniformLocation(int programId, CharSequence name) {
		int location = Uniform.glGetUniformLocation(programId, name);

		if (location == -1 && name.equals("Sampler0")) {
			location = Uniform.glGetUniformLocation(programId, "tex");

			if (location == -1) {
				location = Uniform.glGetUniformLocation(programId, "gtexture");

				if (location == -1) {
					location = Uniform.glGetUniformLocation(programId, "texture");

					// TODO: If a shader samples from *any* sampler with a name that isn't known, then it should act like sampler 0.
				}
			}
		}

		return location;
	}

	@Redirect(method = "updateLocations",
			at = @At(value = "INVOKE", target = "Lorg/apache/logging/log4j/Logger;warn(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V", remap = false))
	private void iris$redirectLogSpam(Logger logger, String message, Object arg1, Object arg2) {
		if (((Object) this) instanceof ExtendedShader) {
			return;
		}

		logger.warn(message, arg1, arg2);
	}
}
