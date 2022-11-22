package net.coderbot.iris.mixin;

import com.google.common.collect.ImmutableList;
import net.coderbot.iris.Iris;
import net.coderbot.iris.compat.sodium.SodiumVersionCheck;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.GraphicsStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.AlertScreen;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.PopupScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;

import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.net.URI;
import java.net.URISyntaxException;

@Mixin(TitleScreen.class)
public class MixinTitleScreen extends Screen {
	private static boolean iris$hasFirstInit;

	protected MixinTitleScreen(Component arg) {
		super(arg);
	}

	@Inject(method = "init", at = @At("RETURN"))
	public void iris$showSodiumIncompatScreen(CallbackInfo ci) {
		if (iris$hasFirstInit) {
			return;
		}

		iris$hasFirstInit = true;

		String reason;

		if (!Iris.isSodiumInstalled() && !FabricLoader.getInstance().isDevelopmentEnvironment()) {
			reason = "iris.sodium.failure.reason.notFound";
		} else if (Iris.isSodiumInvalid()) {
			reason = "iris.sodium.failure.reason.incompatible";
		} else if (Iris.hasNotEnoughCrashes()) {
			Minecraft.getInstance().setScreen(new ConfirmScreen(
				bool -> {
					if (bool) {
						Minecraft.getInstance().setScreen(this);
					} else {
						Minecraft.getInstance().stop();
					}
				},
				Component.translatable("iris.nec.failure.title", Iris.MODNAME).withStyle(ChatFormatting.BOLD, ChatFormatting.RED),
				Component.translatable("iris.nec.failure.description"),
				Component.translatable("options.graphics.warning.accept").withStyle(ChatFormatting.RED),
				Component.translatable("menu.quit").withStyle(ChatFormatting.BOLD)));
			return;
		} else {
			Iris.onLoadingComplete();

			return;
		}

		Minecraft.getInstance().setScreen(new ConfirmScreen(
				(boolean accepted) -> {
					if (accepted) {
						try {
							Util.getPlatform().openUri(new URI(SodiumVersionCheck.getDownloadLink()));
						} catch (URISyntaxException e) {
							throw new IllegalStateException(e);
						}
					} else {
						Minecraft.getInstance().stop();
					}
				},
				Component.translatable("iris.sodium.failure.title").withStyle(ChatFormatting.RED),
				Component.translatable(reason),
				Component.translatable("iris.sodium.failure.download"),
				Component.translatable("menu.quit")));
	}
}
