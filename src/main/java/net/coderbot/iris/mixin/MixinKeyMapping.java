package net.coderbot.iris.mixin;

import net.minecraft.client.KeyMapping;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

/**
 * Maps the key binding categories for Iris to the {@code CATEGORY_SORT_ORDER}, to allow comparisons to work correctly.
 * This is done in a different fashion to the Fabric API for this, to avoid conflicts.
 */
@Mixin(KeyMapping.class)
public class MixinKeyMapping {
	@Shadow
	@Final
	private static Map<String, Integer> CATEGORY_SORT_ORDER;

	@Inject(method = "<clinit>", at = @At("TAIL"))
	private static void iris$addCategory(CallbackInfo ci) {
		CATEGORY_SORT_ORDER.put("iris.keybinds", 8);
	}
}
