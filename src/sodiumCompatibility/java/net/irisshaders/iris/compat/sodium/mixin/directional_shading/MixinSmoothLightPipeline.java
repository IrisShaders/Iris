package net.irisshaders.iris.compat.sodium.mixin.directional_shading;

import me.jellysquid.mods.sodium.client.model.light.data.QuadLightData;
import me.jellysquid.mods.sodium.client.model.light.smooth.SmoothLightPipeline;
import net.irisshaders.iris.shaderpack.materialmap.WorldRenderingSettings;
import net.minecraft.core.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SmoothLightPipeline.class)
public class MixinSmoothLightPipeline {
	@Inject(method = "applySidedBrightness", at = @At("HEAD"), cancellable = true)
	private void iris$disableDirectionalShading(QuadLightData out, Direction face, boolean shade, CallbackInfo ci) {
		if (WorldRenderingSettings.INSTANCE.shouldDisableDirectionalShading()) {
			ci.cancel();
		}
	}
}
