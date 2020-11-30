package net.coderbot.iris.mixin.keybind;

import net.coderbot.iris.Iris;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.TranslatableText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;

//just for keybinds, we could move this to a separate class if needed
@Mixin(MinecraftClient.class)
public class MixinMinecraftClient {

    @Inject(method = "tick", at = @At("TAIL"))
    private void iris$tickKeybinds(CallbackInfo ci) {
        while (Iris.reloadKeybind.wasPressed()) {
            try {
                Iris.reload();
                if (MinecraftClient.getInstance().player != null) {
                    MinecraftClient.getInstance().player.sendMessage(new TranslatableText("iris.shaders.reloaded"), false);
                }
            } catch (IOException e) {
                throw new RuntimeException("Failed to reload shaders!", e);
            }
        }
    }
}
