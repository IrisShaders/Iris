package net.coderbot.iris.mixin.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.coderbot.iris.Iris;
import net.coderbot.iris.gui.screen.HudHideable;
import net.coderbot.iris.pipeline.WorldRenderingPipeline;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(Gui.class)
public class MixinGui {
	@Shadow @Final private Minecraft minecraft;

	@Inject(method = "render", at = @At("HEAD"), cancellable = true)
	public void iris$handleHudHidingScreens(PoseStack poseStack, float tickDelta, CallbackInfo ci) {
		Screen screen = this.minecraft.screen;

		if (screen instanceof HudHideable) {
			ci.cancel();
		}
	}

	// TODO: Move this to a more appropriate mixin
	@Inject(method = "render", at = @At("RETURN"))
	public void iris$displayBigSodiumWarning(PoseStack poseStack, float tickDelta, CallbackInfo ci) {
		if (Iris.isSodiumInstalled()
				|| Minecraft.getInstance().options.renderDebug
				|| !Iris.getCurrentPack().isPresent()) {
			return;
		}

		Font font = Minecraft.getInstance().font;

		List<String> warningLines = new ArrayList<>();
		warningLines.add("[" + Iris.MODNAME + "] Sodium isn't installed; you will have poor performance.");
		warningLines.add("[" + Iris.MODNAME + "] Install Sodium if you want to run benchmarks or get higher FPS!");

		for (int i = 0; i < warningLines.size(); ++i) {
			String string = warningLines.get(i);

			final int lineHeight = 9;
			final int lineWidth = font.width(string);
			final int y = 2 + lineHeight * i;

			GuiComponent.fill(poseStack, 1, y - 1, 2 + lineWidth + 1, y + lineHeight - 1, 0x9050504E);
			font.draw(poseStack, string, 2.0F, y, 0xFFFF55);
		}
	}

	@Inject(method = "renderVignette", at = @At("HEAD"), cancellable = true)
	private void iris$disableVignetteRendering(PoseStack pGui0, Entity pEntity1, CallbackInfo ci) {
		WorldRenderingPipeline pipeline = Iris.getPipelineManager().getPipelineNullable();

		if (pipeline != null && !pipeline.shouldRenderVignette()) {
			// we need to set up the GUI render state ourselves if we cancel the vignette
			RenderSystem.enableDepthTest();
			RenderSystem.defaultBlendFunc();

			ci.cancel();
		}
	}
}
