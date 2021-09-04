package net.coderbot.iris.mixin;

import net.coderbot.iris.pipeline.newshader.ExtendedShader;
import net.minecraft.client.gl.GlUniform;
import net.minecraft.client.render.Shader;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Shader.class)
public class MixinShader {
	@Redirect(method = {"bind()V", "loadReferences()V"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gl/GlUniform;getUniformLocation(ILjava/lang/CharSequence;)I"))
	private int iris$redirectGetUniformLocation(int programId, CharSequence name) {
		int location = GlUniform.getUniformLocation(programId, name);

		if (location == -1 && name.equals("Sampler0")) {
			location = GlUniform.getUniformLocation(programId, "tex");

			if (location == -1) {
				location = GlUniform.getUniformLocation(programId, "gtexture");

				if (location == -1) {
					location = GlUniform.getUniformLocation(programId, "texture");

					// TODO: If a shader samples from *any* sampler with a name that isn't known, then it should act like sampler 0.
				}
			}
		}

		return location;
	}

	@Redirect(method = "loadReferences()V",
			at = @At(value = "INVOKE", target = "org/apache/logging/log4j/Logger.warn (Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V", remap = false))
	private void iris$redirectLogSpam(Logger logger, String message, Object arg1, Object arg2) {
		if (((Object) this) instanceof ExtendedShader) {
			return;
		}

		logger.warn(message, arg1, arg2);
	}
}
