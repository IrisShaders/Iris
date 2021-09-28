package net.coderbot.iris.mixin;

import net.coderbot.iris.Iris;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Ensures that all pipelines are destroyed when changing to a different dimension.
 */
@Mixin(Minecraft.class)
@Environment(EnvType.CLIENT)
public class MixinMinecraft_PipelineDestruction {
	@Inject(method = "setLevel", at = @At("RETURN"))
	private void iris$dispose(ClientLevel world, CallbackInfo ci) {
		Iris.getPipelineManager().destroyPipeline();
	}
}
