package net.irisshaders.iris.mixin.entity_render_context;

import com.llamalad7.mixinextras.sugar.Local;
import net.irisshaders.iris.mixinterface.ModelStorage;
import net.irisshaders.iris.uniforms.CapturedRenderingState;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.OutlineBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SubmitNodeCollection;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Map;

@Mixin(ModelFeatureRenderer.class)
public class MixinModelFeatureRenderer {
	@Inject(method = "renderTranslucents", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/SubmitNodeStorage$TranslucentModelSubmit;modelSubmit()Lnet/minecraft/client/renderer/SubmitNodeStorage$ModelSubmit;"))
	private void iris$set(MultiBufferSource.BufferSource bufferSource, OutlineBufferSource outlineBufferSource, List<SubmitNodeStorage.TranslucentModelSubmit<?>> list, MultiBufferSource.BufferSource bufferSource2, CallbackInfo ci, @Local SubmitNodeStorage.TranslucentModelSubmit<?> modelSubmit) {
		((ModelStorage) (Object) modelSubmit.modelSubmit()).iris$set();
	}

	@Inject(method = "renderBatch", at = @At(value = "INVOKE", target = "Ljava/util/Map$Entry;getKey()Ljava/lang/Object;", ordinal = 1))
	private void iris$set2(MultiBufferSource.BufferSource bufferSource, OutlineBufferSource outlineBufferSource, Map<RenderType, List<SubmitNodeStorage.ModelSubmit<?>>> map, MultiBufferSource.BufferSource bufferSource2, CallbackInfo ci, @Local SubmitNodeStorage.ModelSubmit<?> modelSubmit) {
		((ModelStorage) (Object) modelSubmit).iris$set();
	}

	@Inject(method = "render", at = @At("RETURN"))
	private void iris$clear(SubmitNodeCollection submitNodeCollection, MultiBufferSource.BufferSource bufferSource, OutlineBufferSource outlineBufferSource, MultiBufferSource.BufferSource bufferSource2, CallbackInfo ci) {
		CapturedRenderingState.INSTANCE.setCurrentRenderedItem(0);
		CapturedRenderingState.INSTANCE.setCurrentEntity(0);
		CapturedRenderingState.INSTANCE.setCurrentBlockEntity(0);
	}
}
