package net.coderbot.iris.mixin.fabulous;

import net.coderbot.iris.fantastic.FabulousInterface;
import net.minecraft.client.GraphicsStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Options.class)
public class MixinEnableFabulousGraphics {
	@Shadow
	protected Minecraft minecraft;

	@Shadow
	public GraphicsStatus graphicsMode;

	@Inject(method = "save", at = @At("HEAD"))
	private void onSave(CallbackInfo ci) {
		if (((FabulousInterface) this.minecraft.levelRenderer).wasFabulous()) {
			this.graphicsMode = GraphicsStatus.FABULOUS;
		}
	}

	@Inject(method = "save", at = @At("RETURN"))
	private void afterSave(CallbackInfo ci) {
		if (((FabulousInterface) this.minecraft.levelRenderer).wasFabulous()) {
			this.graphicsMode = GraphicsStatus.FANCY;
		}
	}
}
