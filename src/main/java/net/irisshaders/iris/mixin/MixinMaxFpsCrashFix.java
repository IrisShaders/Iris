package net.irisshaders.iris.mixin;

import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;

/**
 * A workaround for when OptiFine has set the maxFps to zero in options.txt
 * <p>
 * Fun.
 */
@Mixin(Options.class)
public abstract class MixinMaxFpsCrashFix {
	@Redirect(
		method = "processOptions",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Options$FieldAccess;process(Ljava/lang/String;Lnet/minecraft/client/OptionInstance;)V"),
		slice = @Slice(from = @At(value = "CONSTANT", args = "stringValue=maxFps"), to = @At(value = "CONSTANT", args = "stringValue=graphicsMode")),
		allow = 1
	)
	private void iris$resetFramerateLimit(Options.FieldAccess instance, String name, OptionInstance<Integer> option) {
		if (option.get() == 0) {
			// Return the default value of framerateLimit
			option.set(120);
		}

		instance.process(name, option);
	}
}
