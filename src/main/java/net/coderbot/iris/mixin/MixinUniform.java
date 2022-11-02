package net.coderbot.iris.mixin;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Tries to ensure that texture unit 0 ends up as the semantically default texture unit with Iris extended shaders.
 *
 * Located in {@link Uniform} to avoid a conflict with a Sodium mixin to ShaderInstance.
 */
@Mixin(Uniform.class)
public class MixinUniform {
	@Inject(method = "glGetUniformLocation", at = @At("RETURN"), cancellable = true)
	private static void iris$glGetUniformLocation(int programId, CharSequence name,
												  CallbackInfoReturnable<Integer> cir) {
		int location = cir.getReturnValue();

		if (location == -1 && name.equals("Sampler0")) {
			location = GlStateManager._glGetUniformLocation(programId, "tex");

			if (location == -1) {
				location = GlStateManager._glGetUniformLocation(programId, "gtexture");

				if (location == -1) {
					location = GlStateManager._glGetUniformLocation(programId, "texture");

					// TODO: If a shader samples from *any* sampler with a name that isn't known, then it should act like sampler 0.
				}
			}
		}

		if (cir.getReturnValue() == -1 && location != -1) {
			cir.setReturnValue(location);
		}
	}
}
