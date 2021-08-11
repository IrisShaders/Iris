package net.coderbot.iris.mixin.shadows;

import net.coderbot.iris.pipeline.ShadowRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.WorldRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Prevent nearby chunks from being rebuilt on the main thread in the shadow pass. Aside from causing  FPS to tank,
 * this also causes weird chunk corruption! It's critical to make sure that it's disabled as a result.
 * <p>
 * This patch is not relevant with Sodium installed since Sodium has a completely different build path for terrain
 * setup.
 */
@Mixin(WorldRenderer.class)
public class MixinPreventRebuildNearInShadowPass {
	private static final String PROFILER_SWAP = "Lnet/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V";

	@Inject(method = "setupTerrain", at = @At(value = "INVOKE_STRING", target = PROFILER_SWAP, args = "ldc=rebuildNear"), cancellable = true)
	private void iris$preventRebuildNearInShadowPass(Camera camera, Frustum frustum, boolean hasForcedFrustum, int frame, boolean spectator, CallbackInfo callback) {
		if (ShadowRenderer.ACTIVE) {
			MinecraftClient.getInstance().getProfiler().pop();
			callback.cancel();
		}
	}
}
