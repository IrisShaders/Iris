package net.irisshaders.iris.mixin.shadows;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.irisshaders.iris.shadows.ShadowRenderer;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import net.minecraft.client.renderer.culling.Frustum;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Prevent nearby chunks from being rebuilt on the main thread in the shadow pass. Aside from causing FPS to tank,
 * this also causes weird chunk corruption! It's critical to make sure that it's disabled as a result.
 * <p>
 * This patch is not relevant with Sodium installed since Sodium has a completely different build path for terrain
 * setup.
 * <p>
 * Uses a priority of 1010 to apply after Sodium's overwrite, to allow for the Group behavior to activate. Otherwise,
 * if we apply with the same priority, then we'll just get a Mixin error due to the injects conflicting with the
 * {@code @Overwrite}. Using {@code @Group} allows us to avoid a fragile Mixin plugin.
 */
@Mixin(value = LevelRenderer.class, priority = 1010)
public abstract class MixinPreventRebuildNearInShadowPass {
	@Shadow
	@Final
	private ObjectArrayList<SectionRenderDispatcher.RenderSection> visibleSections;

	@Inject(method = "setupRender",
		at = @At(value = "TAIL"))
	private void iris$preventRebuildNearInShadowPass(Camera camera, Frustum frustum, boolean bl, boolean bl2, CallbackInfo ci) {
		if (ShadowRenderer.ACTIVE) {
			for (SectionRenderDispatcher.RenderSection chunk : this.visibleSections) {
				ShadowRenderer.visibleBlockEntities.addAll(chunk.getCompiled().getRenderableBlockEntities());
			}
		}
	}
}
