package net.irisshaders.iris.mixin;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.preprocessor.GlslPreprocessor;
import com.mojang.blaze3d.shaders.Program;
import net.irisshaders.iris.gl.shader.ShaderCompileException;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;

@Mixin(Program.class)
public class MixinProgram {
	@Redirect(method = "compileShaderInternal", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/preprocessor/GlslPreprocessor;process(Ljava/lang/String;)Ljava/util/List;"))
	private static List<String> iris$allowSkippingMojImportDirectives(GlslPreprocessor includeHandler, String shaderSource) {
		// Mojang's code for handling #moj_import directives uses regexes that can cause StackOverflowErrors.
		//
		// Rather than fix the crash, we just don't try to process directives if they don't exist, which is fine
		// for Iris since we don't allow using moj_import.
		if (!shaderSource.contains("moj_import")) {
			return Collections.singletonList(shaderSource);
		}

		return includeHandler.process(shaderSource);
	}

	@Inject(method = "compileShaderInternal", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/GlStateManager;glGetShaderInfoLog(II)Ljava/lang/String;"), locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
	private static void iris$causeException(Program.Type arg, String string, InputStream inputStream, String string2, GlslPreprocessor arg2, CallbackInfoReturnable<Integer> cir, String string3, int i) {
		cir.cancel();
		throw new ShaderCompileException(string + arg.getExtension(), GlStateManager.glGetShaderInfoLog(i, 32768));
	}
}
