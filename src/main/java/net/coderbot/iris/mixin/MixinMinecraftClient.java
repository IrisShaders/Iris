package net.coderbot.iris.mixin;

import net.coderbot.iris.Iris;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public abstract class MixinMinecraftClient {
    @Shadow @Nullable public ClientPlayerEntity player;

    @Shadow @Final public WorldRenderer worldRenderer;

    @Inject(method = "tick", at = @At("TAIL"))
    private void iris$tickKeybind(CallbackInfo ci) {
        while (Iris.reloadKeybind.wasPressed()){
            try {
                Iris.reload();
                this.worldRenderer.reload();
                if (this.player != null){
                    this.player.sendMessage(new TranslatableText("iris.shaders.reloaded"), false);
                }
            } catch (Exception e) {
                Iris.logger.error("Error while reloading Shaders for Iris!", e);
                if (this.player != null) {
                    this.player.sendMessage(new TranslatableText("iris.shaders.reloaded.failure", getRootCause(e).getMessage()).formatted(Formatting.RED), false);
                }
            }
        }


    }

    private Throwable getRootCause(Throwable throwable) {
        Throwable copy = throwable;
        while (copy.getCause() != null && copy.getCause() != copy){
            copy = copy.getCause();
        }
        return copy;
    }

}
