package net.coderbot.iris.mixin;

import net.coderbot.iris.Iris;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.TranslatableText;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;

@Mixin(MinecraftClient.class)
public class MixinMinecraftClient {
    @Shadow @Nullable public ClientPlayerEntity player;

    @Inject(method = "tick", at = @At("TAIL"))
    private void iris$tickKeybind(CallbackInfo ci) {
        while (Iris.reloadKeybind.wasPressed()){
            try {
                Iris.reload();
                if (this.player != null){
                    this.player.sendMessage(new TranslatableText("iris.shaders.reloaded"), false);
                }
            } catch (IOException e) {
                Iris.logger.error("Error while reloading Shaders for Iris!", e);
            }
        }

    }
}
