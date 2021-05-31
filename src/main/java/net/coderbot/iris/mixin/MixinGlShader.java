package net.coderbot.iris.mixin;

import net.minecraft.class_5913;
import net.minecraft.client.gl.GlShader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Collections;
import java.util.List;

@Mixin(GlShader.class)
public class MixinGlShader {
	@Redirect(method = "method_34416", at = @At(value = "INVOKE", target = "net/minecraft/class_5913.method_34229 (Ljava/lang/String;)Ljava/util/List;"))
	private static List<String> iris$allowSkippingMojImportDirectives(class_5913 includeHandler, String shaderSource) {
		// Mojang's code for handling #moj_import directives uses regexes that can cause StackOverflowErrors.
		//
		// Rather than fix the crash, we just don't try to process directives if they don't exist, which is fine
		// for Iris since we don't allow using moj_import.
		if (!shaderSource.contains("moj_import")) {
			return Collections.singletonList(shaderSource);
		}

		return includeHandler.method_34229(shaderSource);
	}
}
