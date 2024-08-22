package net.irisshaders.iris.mixin;

import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * A workaround for when OptiFine has set the maxFps to zero in options.txt
 * <p>
 * Fun.
 */
@Mixin(Options.class)
public abstract class MixinMaxFpsCrashFix {
	@Unique
	private void iris$resetFramerateLimit(Options.FieldAccess instance, String name, OptionInstance<Integer> option) {
		if (option.get() == 0) {
			// Return the default value of framerateLimit
			option.set(120);
		}

		instance.process(name, option);
	}
}
