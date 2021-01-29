package net.coderbot.iris.mixin;

import net.coderbot.iris.gui.TransparentBackgroundScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class MixinInGameHud {
    @Shadow @Final private MinecraftClient client;

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    public void handleTransparentScreens(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
        Screen screen = this.client.currentScreen;
        if(screen instanceof TransparentBackgroundScreen && !((TransparentBackgroundScreen)screen).renderHud()) ci.cancel();
    }
}
