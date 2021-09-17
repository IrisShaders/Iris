package net.coderbot.iris.mixin.shadows;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.coderbot.iris.pipeline.ShadowRenderer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Prevent nearby chunks from being rebuilt on the main thread in the shadow pass. Aside from causing FPS to tank,
 * this also causes weird chunk corruption! It's critical to make sure that it's disabled as a result.
 *
 * This patch is not relevant with Sodium installed since Sodium has a completely different build path for terrain
 * setup.
 */
@Mixin(LevelRenderer.class)
public abstract class MixinPreventRebuildNearInShadowPass {
	@Shadow
	@Final
	private ObjectArrayList<LevelRenderer.RenderChunkInfo> renderChunksInFrustum;

	@Shadow
	protected abstract void applyFrustum(Frustum frustum);

	@Inject(method = "setupRender", at = @At(value = "INVOKE", target = "Ljava/util/concurrent/atomic/AtomicReference;get()Ljava/lang/Object;"), cancellable = true)
	private void iris$preventRebuildNearInShadowPass(Camera camera, Frustum frustum, boolean bl, boolean bl2, CallbackInfo ci) {
		if (ShadowRenderer.ACTIVE) {
			for (LevelRenderer.RenderChunkInfo chunk : this.renderChunksInFrustum) {
				for (BlockEntity entity : ((ChunkInfoAccessor) chunk).getChunk().getCompiledChunk().getRenderableBlockEntities()) {
					ShadowRenderer.visibleBlockEntities.add(entity);
				}
			}
			Minecraft.getInstance().getProfiler().pop();
			this.applyFrustum(frustum);
			ci.cancel();
		}
	}
}
