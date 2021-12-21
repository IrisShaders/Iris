package net.coderbot.iris.mixin;

import com.google.common.collect.ImmutableSet;
import com.mojang.blaze3d.shaders.Uniform;
import net.coderbot.iris.gl.IrisRenderSystem;
import net.coderbot.iris.pipeline.newshader.ExtendedShader;
import net.coderbot.iris.pipeline.newshader.fallback.FallbackShader;
import net.minecraft.client.renderer.ShaderInstance;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.ARBTextureSwizzle;
import org.lwjgl.opengl.GL20C;
import org.lwjgl.opengl.GL30C;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Objects;

@Mixin(ShaderInstance.class)
public class MixinShaderInstance {
	@Unique
	private String lastSamplerName;

	@Unique
	private static final ImmutableSet<String> ATTRIBUTE_LIST = ImmutableSet.of("Position", "Color", "Normal", "UV0", "UV1", "UV2");

	@Inject(method = "apply",
			at = @At(value = "INVOKE", target = "com/mojang/blaze3d/systems/RenderSystem.bindTexture (I)V",
					remap = false),
			locals = LocalCapture.CAPTURE_FAILHARD)
	private void iris$beforeBindTexture(CallbackInfo ci, int lastActiveTexture, int textureUnit, String samplerName) {
		// Need to do this here since the LVT changes after the bindTexture call.
		lastSamplerName = samplerName;
	}

	@Inject(method = "apply",
			at = @At(value = "INVOKE", target = "com/mojang/blaze3d/systems/RenderSystem.bindTexture (I)V",
					remap = false, shift = At.Shift.AFTER))
	private void iris$afterBindTexture(CallbackInfo ci) {
		final String samplerName = Objects.requireNonNull(lastSamplerName);
		lastSamplerName = null;

		if (!(((Object) this) instanceof ExtendedShader)) {
			return;
		}

		if (!((ExtendedShader) (Object) this).isIntensitySwizzle()) {
			return;
		}

		// TODO: Don't hardcode this list of samplers bound to texture unit 0
		if (samplerName.equals("Sampler0") || samplerName.equals("tex") || samplerName.equals("texture")
				|| samplerName.equals("gtexture")) {
			// Mimic the texture(..., ...).rrrr swizzle behavior that the text_intensity shader (the shader used for
			// TTF fonts) needs outside of shader code to avoid having to do complex shader patching.
			//
			// In 1.16.5 and below, shaders worked out of the box with TTF fonts, since Minecraft was able to use the
			// deprecated GL_INTENSITY image format. Using a Swizzle Mask allows us to replicate the functionality of
			// this format on the core profile using GL_RED:
			//
			// https://www.khronos.org/opengl/wiki/Image_Format#Legacy_Image_Formats
			// https://www.khronos.org/opengl/wiki/Texture#Swizzle_mask

			// TODO: Avoid direct GL calls
			IrisRenderSystem.texParameteriv(GL20C.GL_TEXTURE_2D, ARBTextureSwizzle.GL_TEXTURE_SWIZZLE_RGBA,
					new int[] { GL30C.GL_RED, GL30C.GL_RED, GL30C.GL_RED, GL30C.GL_RED });
		}
	}

	@Redirect(method = "updateLocations",
			at = @At(value = "INVOKE", target = "Lorg/apache/logging/log4j/Logger;warn(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V", remap = false))
	private void iris$redirectLogSpam(Logger logger, String message, Object arg1, Object arg2) {
		if (((Object) this) instanceof ExtendedShader || ((Object) this) instanceof FallbackShader) {
			return;
		}

		logger.warn(message, arg1, arg2);
	}

	@Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/shaders/Uniform;glBindAttribLocation(IILjava/lang/CharSequence;)V"))
	public void iris$redirectBindAttributeLocation(int i, int j, CharSequence charSequence) {
		if (((Object) this) instanceof ExtendedShader && ATTRIBUTE_LIST.contains(charSequence)) {
			Uniform.glBindAttribLocation(i, j, "iris_" + charSequence);
		} else {
			Uniform.glBindAttribLocation(i, j, charSequence);
		}
	}
}
