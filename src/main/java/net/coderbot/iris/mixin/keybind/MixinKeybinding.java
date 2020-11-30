package net.coderbot.iris.mixin.keybind;

import net.minecraft.client.options.KeyBinding;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(KeyBinding.class)
public class MixinKeybinding {
    @Shadow @Final private static Map<String, Integer> categoryOrderMap;

    /**
     * This is used to add a new iris category for iris keybindings
     * this could be removed and iris keybindings could instead be moved to
     * a vanilla category
     */
    @SuppressWarnings("UnresolvedMixinReference")
    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void iris$addIrisKeybindingCategory(CallbackInfo ci) {
        categoryOrderMap.put("iris.keybinds", 8);
    }
}
