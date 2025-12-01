package net.irisshaders.iris.mixin.entity_render_context;

import com.llamalad7.mixinextras.sugar.Local;
import net.irisshaders.iris.layer.GbufferPrograms;
import net.irisshaders.iris.mixinterface.ModelStorage;
import net.irisshaders.iris.uniforms.CapturedRenderingState;
import net.irisshaders.iris.vertices.ImmediateState;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.OutlineBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollection;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.feature.ModelPartFeatureRenderer;
import net.minecraft.client.renderer.feature.TextFeatureRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TextFeatureRenderer.class)
public class MixinTextFeatureRenderer {
	@Unique
	private boolean hasBE = false;

	@Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/SubmitNodeStorage$TextSubmit;outlineColor()I", ordinal = 0))
	private void iris$set(SubmitNodeCollection submitNodeCollection, MultiBufferSource.BufferSource bufferSource, CallbackInfo ci, @Local SubmitNodeStorage.TextSubmit modelSubmit) {
		((ModelStorage) (Object) modelSubmit).iris$set();
		if (((ModelStorage) (Object) modelSubmit).iris$wasBE()) {
			hasBE = true;
			ImmediateState.isRenderingBEs = true;
		} else if (hasBE) {
			hasBE = false;
			ImmediateState.isRenderingBEs = false;
		}
	}

	@Inject(method = "render", at = @At("RETURN"))
	private void iris$clear(SubmitNodeCollection submitNodeCollection, MultiBufferSource.BufferSource bufferSource, CallbackInfo ci) {
		CapturedRenderingState.INSTANCE.setCurrentRenderedItem(0);
		CapturedRenderingState.INSTANCE.setCurrentEntity(0);
		CapturedRenderingState.INSTANCE.setCurrentBlockEntity(0);
		hasBE = false;
		ImmediateState.isRenderingBEs = false;
	}
}
