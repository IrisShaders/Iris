package net.coderbot.iris.mixin;

import net.minecraft.client.Options;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;

/**
 * A workaround for when OptiFine has set the maxFps to zero in options.txt
 * 
 * Fun.
 */
@Mixin(Options.class)
public abstract class MixinMaxFpsCrashFix {
	@Redirect(
		method = "processOptions",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Options$FieldAccess;process(Ljava/lang/String;I)I"),
		slice = @Slice(from = @At(value = "CONSTANT", args = "stringValue=maxFps"), to = @At(value = "CONSTANT", args = "stringValue=difficulty")),
		allow = 1
	)
	private int iris$resetFramerateLimit(Options.FieldAccess instance, String name, int nullValue) {
		int original = instance.process(name, nullValue);

		if (original == 0) {
			// Return the default value of framerateLimit
			return 120;
		}

		return original;
	}
}
