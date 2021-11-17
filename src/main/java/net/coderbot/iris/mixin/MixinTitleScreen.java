package net.coderbot.iris.mixin;

import net.coderbot.iris.Iris;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.AlertScreen;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.TranslatableComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.net.URI;
import java.net.URISyntaxException;

@Mixin(TitleScreen.class)
public class MixinTitleScreen {
	@Inject(method = "init", at = @At("RETURN"))
	public void iris$showSodiumIncompatScreen(CallbackInfo ci) {
		String reason;

		if (!Iris.isSodiumInstalled() && !FabricLoader.getInstance().isDevelopmentEnvironment()) {
			reason = "iris.sodium.failure.reason.notFound";
		} else if (Iris.isSodiumInvalid()) {
			reason = "iris.sodium.failure.reason.incompatible";
		} else {
			return;
		}

		if (Iris.isSodiumInvalid()) {
			Minecraft.getInstance().setScreen(new AlertScreen(
					Minecraft.getInstance()::stop,
					new TranslatableComponent("iris.sodium.failure.title").withStyle(ChatFormatting.RED),
					new TranslatableComponent("iris.sodium.failure.reason"),
					new TranslatableComponent("menu.quit")));
		}

		Minecraft.getInstance().setScreen(new ConfirmScreen(
				(boolean accepted) -> {
					if (accepted) {
						try {
							Util.getPlatform().openUri(new URI("https://www.curseforge.com/minecraft/mc-mods/sodium/files/3488836"));
						} catch (URISyntaxException e) {
							throw new IllegalStateException(e);
						}
					} else {
						Minecraft.getInstance().stop();
					}
				},
				new TranslatableComponent("iris.sodium.failure.title").withStyle(ChatFormatting.RED),
				new TranslatableComponent(reason),
				new TranslatableComponent("iris.sodium.failure.download"),
				new TranslatableComponent("menu.quit")));
	}
}
