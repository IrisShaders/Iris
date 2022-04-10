package net.coderbot.iris.mixin.shadows;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
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
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Prevent nearby chunks from being rebuilt on the main thread in the shadow pass. Aside from causing FPS to tank,
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
public abstract class MixinPreventRebuildNearInShadowPass {
	@Shadow
	@Final
	private ObjectArrayList<LevelRenderer.RenderChunkInfo> renderChunksInFrustum;

	@Shadow
	protected abstract void applyFrustum(Frustum frustum);

	@Group(name = "iris_MixinPreventRebuildNearInShadowPass", min = 1, max = 1)
	@Inject(method = "setupRender",
			at = @At(value = "INVOKE",
					target = "Ljava/util/concurrent/atomic/AtomicReference;get()Ljava/lang/Object;"),
			cancellable = true,
			require = 0)
	private void iris$preventRebuildNearInShadowPass(Camera camera, Frustum frustum, boolean bl, boolean bl2, CallbackInfo ci) {
		if (ShadowRenderer.ACTIVE) {
			for (LevelRenderer.RenderChunkInfo chunk : this.renderChunksInFrustum) {
				ShadowRenderer.visibleBlockEntities.addAll(((ChunkInfoAccessor) chunk).getChunk().getCompiledChunk().getRenderableBlockEntities());
			}
			Minecraft.getInstance().getProfiler().pop();
			this.applyFrustum(frustum);
			ci.cancel();
		}
	}

	@Group(name = "iris_MixinPreventRebuildNearInShadowPass", min = 1, max = 1)
	@Inject(method = "setupRender",
			at = @At(value = "INVOKE",
					target = "me/jellysquid/mods/sodium/client/gl/device/RenderDevice.enterManagedCode ()V",
					remap = false),
			require = 0)
	private void iris$cannotInject(Camera camera, Frustum frustum, boolean bl, boolean bl2, CallbackInfo ci) {
		// Dummy injection just to assert that either Sodium is present, or the vanilla injection passed.
	}
}
