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
import net.minecraft.client.gui.GuiGraphics;
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
	public void iris$handleHudHidingScreens(GuiGraphics pGui0, float pFloat1, CallbackInfo ci) {
		Screen screen = this.minecraft.screen;

		if (screen instanceof HudHideable) {
			ci.cancel();
		}
	}

	@Inject(method = "renderVignette", at = @At("HEAD"), cancellable = true)
	private void iris$disableVignetteRendering(GuiGraphics pGui0, Entity pEntity1, CallbackInfo ci) {
		WorldRenderingPipeline pipeline = Iris.getPipelineManager().getPipelineNullable();

		if (pipeline != null && !pipeline.shouldRenderVignette()) {
			// we need to set up the GUI render state ourselves if we cancel the vignette
			RenderSystem.enableDepthTest();
			RenderSystem.defaultBlendFunc();

			ci.cancel();
		}
	}
}
