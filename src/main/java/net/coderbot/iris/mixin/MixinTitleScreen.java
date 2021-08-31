package net.coderbot.iris.mixin;

import net.coderbot.iris.Iris;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.FatalErrorScreen;
import net.minecraft.client.gui.screen.NoticeScreen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public class MixinTitleScreen {
	@Inject(method = "init", at = @At("RETURN"))
	public void iris$showSodiumIncompatScreen(CallbackInfo ci) {
		if(Iris.isSodiumInvalid()) {
			MinecraftClient.getInstance().setScreen(new NoticeScreen(() -> {
				MinecraftClient.getInstance().scheduleStop();
			}, new TranslatableText("iris.sodium.failure.title").formatted(Formatting.RED), new TranslatableText("iris.sodium.failure.reason"), new TranslatableText("menu.quit")));
		}
	}
}
