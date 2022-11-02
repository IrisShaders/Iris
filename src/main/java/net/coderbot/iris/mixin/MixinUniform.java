package net.coderbot.iris.mixin;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.coderbot.iris.pipeline.newshader.uniforms.RedirectingUniform3F;
import net.coderbot.iris.pipeline.newshader.uniforms.RedirectingUniform4F;
import net.coderbot.iris.pipeline.newshader.uniforms.RedirectingUniformMatrix;
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

	@Inject(method = "set(Lcom/mojang/math/Matrix4f;)V", at = @At("HEAD"), cancellable = true)
	private void cancelUniform(Matrix4f arg, CallbackInfo ci) {
		if ((Object) this instanceof RedirectingUniformMatrix) {
			((RedirectingUniformMatrix) (Object) this).setOverride(arg);
			ci.cancel();
		}
	}

	@Inject(method = "set(FFF)V", at = @At("HEAD"), cancellable = true)
	private void cancelUniform3F(float f, float g, float h, CallbackInfo ci) {
		if ((Object) this instanceof RedirectingUniform3F) {
			((RedirectingUniform3F) (Object) this).setOverride(f, g, h);
			ci.cancel();
		}
	}

	@Inject(method = "set(Lcom/mojang/math/Vector3f;)V", at = @At("HEAD"), cancellable = true)
	private void cancelUniform3F2(Vector3f arg, CallbackInfo ci) {
		if ((Object) this instanceof RedirectingUniform3F) {
			((RedirectingUniform3F) (Object) this).setOverride(arg.x(), arg.y(), arg.z());
			ci.cancel();
		}
	}

	@Inject(method = "set([F)V", at = @At("HEAD"), cancellable = true)
	private void cancelUniform4F(float[] fs, CallbackInfo ci) {
		if ((Object) this instanceof RedirectingUniform4F && fs.length == 4) {
			((RedirectingUniform4F) (Object) this).setOverride(fs[0], fs[1], fs[2], fs[3]);
			ci.cancel();
		}
	}

	@Inject(method = "set(FFFF)V", at = @At("HEAD"), cancellable = true)
	private void cancelUniform4F2(float f, float g, float h, float i, CallbackInfo ci) {
		if ((Object) this instanceof RedirectingUniform4F) {
			((RedirectingUniform4F) (Object) this).setOverride(f, g, h, i);
			ci.cancel();
		}
	}
}
