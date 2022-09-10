package net.coderbot.iris.mixin;

import net.minecraft.client.KeyMapping;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.Optional;

/**
 * Maps the key binding categories for Iris to the {@code CATEGORY_SORT_ORDER}, to allow comparisons to work correctly.
 * This is done after Fabric API.
 */
@Mixin(value = KeyMapping.class, priority = 1010)
public class MixinKeyMapping {
	@Shadow
	@Final
	private static Map<String, Integer> CATEGORY_SORT_ORDER;

	@Inject(method = "<clinit>", at = @At("TAIL"))
	private static void iris$addCategory(CallbackInfo ci) {
		if (CATEGORY_SORT_ORDER.containsKey("iris.keybinds")) {
			return;
		}

		Optional<Integer> largest = CATEGORY_SORT_ORDER.values().stream().max(Integer::compareTo);
		int largestInt = largest.orElse(0);
		CATEGORY_SORT_ORDER.put("iris.keybinds", largestInt + 1);
	}
}
