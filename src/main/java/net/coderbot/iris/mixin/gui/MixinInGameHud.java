package net.coderbot.iris.mixin.gui;

import net.coderbot.iris.Iris;
import net.coderbot.iris.gui.screen.HudHideable;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(InGameHud.class)
public class MixinInGameHud {
	@Shadow @Final private MinecraftClient client;

	@Inject(method = "render", at = @At("HEAD"), cancellable = true)
	public void iris$handleHudHidingScreens(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
		Screen screen = this.client.currentScreen;

		if (screen instanceof HudHideable) {
			ci.cancel();
		}
	}

	// TODO: Move this to a more appropriate mixin
	@Inject(method = "render", at = @At("RETURN"))
	public void iris$displayBigSodiumWarning(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
		if (Iris.isSodiumInstalled()
				|| MinecraftClient.getInstance().options.debugEnabled
				|| !Iris.getCurrentPack().isPresent()) {
			return;
		}

		TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

		List<String> warningLines = new ArrayList<>();
		warningLines.add("[Iris] Sodium isn't installed; you will have poor performance.");
		warningLines.add("[Iris] Install the compatible Sodium fork if you want to run benchmarks or get higher FPS!");

		for(int i = 0; i < warningLines.size(); ++i) {
			String string = warningLines.get(i);

			final int lineHeight = 9;
			final int lineWidth = textRenderer.getWidth(string);
			final int y = 2 + lineHeight * i;

			DrawableHelper.fill(matrices, 1, y - 1, 2 + lineWidth + 1, y + lineHeight - 1, 0x9050504E);
			textRenderer.draw(matrices, string, 2.0F, y, 0xFFFF55);
		}
	}
}
