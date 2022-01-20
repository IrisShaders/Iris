package net.coderbot.iris.mixin;

import net.minecraft.client.gui.screens.ReceivingLevelScreen;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ReceivingLevelScreen.class)
public class MixinRecievingLevelScreen {
	@Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;isChunkCompiled(Lnet/minecraft/core/BlockPos;)Z"))
	private boolean disableCompileWait(LevelRenderer instance, BlockPos blockPos) {
		// TODO: This shouldn't be causing the game to freeze, why is it?
		return true;
	}
}
