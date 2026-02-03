package net.irisshaders.iris.mixin.entity_render_context;

import net.irisshaders.iris.shaderpack.materialmap.NamespacedId;
import net.irisshaders.iris.shaderpack.materialmap.WorldRenderingSettings;
import net.irisshaders.iris.uniforms.CapturedRenderingState;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollection;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.feature.FlameFeatureRenderer;
import net.minecraft.client.resources.model.AtlasManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FlameFeatureRenderer.class)
public class MixinFlameFeatureRenderer {
	@Unique
	private static final NamespacedId flameId = new NamespacedId("minecraft", "entity_flame");

	@Inject(method = "render", at = @At("HEAD"))
	private void iris$setFlame(SubmitNodeCollection submitNodeCollection, MultiBufferSource.BufferSource bufferSource, AtlasManager atlasManager, CallbackInfo ci) {
		if (WorldRenderingSettings.INSTANCE.getEntityIds() != null) {
			CapturedRenderingState.INSTANCE.setCurrentEntity(WorldRenderingSettings.INSTANCE.getEntityIds().applyAsInt(flameId));
		}
	}

	@Inject(method = "render", at = @At("RETURN"))
	private void iris$setFlame2(SubmitNodeCollection submitNodeCollection, MultiBufferSource.BufferSource bufferSource, AtlasManager atlasManager, CallbackInfo ci) {
		CapturedRenderingState.INSTANCE.setCurrentEntity(0);

	}
}
