package net.irisshaders.iris.mixin.entity_render_context;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.irisshaders.iris.shaderpack.materialmap.NamespacedId;
import net.irisshaders.iris.shaderpack.materialmap.WorldRenderingSettings;
import net.irisshaders.iris.uniforms.CapturedRenderingState;
import net.minecraft.client.renderer.feature.FlameFeatureRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FlameFeatureRenderer.class)
public class MixinFlameFeatureRenderer {
	@Unique
	private static final NamespacedId flameId = new NamespacedId("minecraft", "entity_flame");

	@Inject(method = "prepare", at = @At("HEAD"))
	private void iris$setFlame(FlameFeatureRenderer.Submit submit, VertexConsumer buffer, TextureAtlasSprite fire1,
		TextureAtlasSprite fire2, CallbackInfo ci) {
		if (WorldRenderingSettings.INSTANCE.getEntityIds() != null) {
			CapturedRenderingState.INSTANCE.setCurrentEntity(WorldRenderingSettings.INSTANCE.getEntityIds().applyAsInt(flameId));
		}
	}

	@Inject(method = "prepare", at = @At("RETURN"))
	private void iris$setFlame2(FlameFeatureRenderer.Submit submit, VertexConsumer buffer, TextureAtlasSprite fire1,
		TextureAtlasSprite fire2, CallbackInfo ci) {
		CapturedRenderingState.INSTANCE.setCurrentEntity(0);

	}
}
