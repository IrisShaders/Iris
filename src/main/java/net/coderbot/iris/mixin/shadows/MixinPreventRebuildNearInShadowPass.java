package net.coderbot.iris.mixin.shadows;

import it.unimi.dsi.fastutil.objects.ObjectList;
import net.coderbot.iris.pipeline.ShadowRenderer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.culling.Frustum;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Group;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Prevent nearby chunks from being rebuilt on the main thread in the shadow pass. Aside from causing  FPS to tank,
 * this also causes weird chunk corruption! It's critical to make sure that it's disabled as a result.
 *
 * This patch is not relevant with Sodium installed since Sodium has a completely different build path for terrain
 * setup.
 *
 * Uses a priority of 1010 to apply after Sodium's overwrite, to allow for the Group behavior to activate. Otherwise,
 * if we apply with the same priority, then we'll just get a Mixin error due to the injects conflicting with the
 * {@code @Overwrite}. Using {@code @Group} allows us to avoid a fragile Mixin plugin.
 */
@Mixin(value = LevelRenderer.class, priority = 1010)
public class MixinPreventRebuildNearInShadowPass {
	@Shadow
	@Final
	private ObjectList<LevelRenderer.RenderChunkInfo> renderChunks;

	@Group(name = "iris_MixinPreventRebuildNearInShadowPass", min = 1, max = 1)
	@Inject(method = "setupRender",
			at = @At(value = "INVOKE_STRING",
					target = "Lnet/minecraft/util/profiling/ProfilerFiller;popPush(Ljava/lang/String;)V",
					args = "ldc=rebuildNear"),
			cancellable = true,
			require = 0)
	private void iris$preventRebuildNearInShadowPass(Camera camera, Frustum frustum, boolean hasForcedFrustum,
													 int frame, boolean spectator, CallbackInfo callback) {
		if (ShadowRenderer.ACTIVE) {
			for (LevelRenderer.RenderChunkInfo chunk : this.renderChunks) {
				ShadowRenderer.visibleBlockEntities.addAll(((ChunkInfoAccessor) chunk).getChunk().getCompiledChunk().getRenderableBlockEntities());
			}
			Minecraft.getInstance().getProfiler().pop();
			callback.cancel();
		}
	}

	@Group(name = "iris_MixinPreventRebuildNearInShadowPass", min = 1, max = 1)
	@Inject(method = "setupRender",
			at = @At(value = "INVOKE",
					target = "me/jellysquid/mods/sodium/client/gl/device/RenderDevice.enterManagedCode ()V",
					remap = false),
			require = 0)
	private void iris$cannotInject(Camera camera, Frustum frustum, boolean hasForcedFrustum, int frame,
								   boolean spectator, CallbackInfo callback) {
		// Dummy injection just to assert that either Sodium is present, or the vanilla injection passed.
	}
}
