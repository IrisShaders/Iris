package net.coderbot.iris.mixin.keybind;

import com.google.common.collect.Lists;
import net.coderbot.iris.Iris;
import net.minecraft.client.options.GameOptions;
import net.minecraft.client.options.KeyBinding;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(GameOptions.class)
public class MixinGameOptions {
    @Mutable
    @Shadow @Final public KeyBinding[] keysAll;

    @Inject(method = "load", at = @At("HEAD"))
    private void iris$addKeybind(CallbackInfo ci) {
        List<KeyBinding> currentBindings = Lists.newArrayList(this.keysAll);
        currentBindings.add(Iris.reloadKeybind);
        keysAll = new KeyBinding[currentBindings.size()];
        currentBindings.toArray(keysAll);
    }
}
