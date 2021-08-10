package net.coderbot.iris.mixin;

import net.coderbot.iris.Iris;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.FatalErrorScreen;
import net.minecraft.client.gui.screen.NoticeScreen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public class MixinTitleScreen {
	@Inject(method = "init", at = @At("RETURN"))
	public void showError(CallbackInfo ci) {
		if(Iris.sodiumInvalid) {
			MinecraftClient.getInstance().openScreen(new NoticeScreen(() -> {
				MinecraftClient.getInstance().scheduleStop();
			}, new LiteralText("Iris failed to load!").formatted(Formatting.RED), new LiteralText("You have Sodium in your mods folder, however Iris includes it. Please close the game, and remove Sodium from your mods folder.")));
		}
	}
}
