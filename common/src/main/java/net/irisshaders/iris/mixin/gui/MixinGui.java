package net.irisshaders.iris.mixin.gui;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.irisshaders.iris.gui.screen.HudHideable;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Hud.class)
public class MixinGui {
	@Shadow
	@Final
	private Minecraft minecraft;

	@WrapMethod(method = "extractRenderState")
	public void iris$handleHudHidingScreens(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, Operation<Void> original) {
		Screen screen = this.minecraft.gui.screen();

		if (screen instanceof HudHideable) {
			return;
		}


		original.call(graphics, deltaTracker);

	}
}
