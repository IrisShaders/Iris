package net.irisshaders.iris.mixin.entity_render_context;

import com.llamalad7.mixinextras.sugar.Local;
import net.irisshaders.iris.mixinterface.ModelStorage;
import net.irisshaders.iris.uniforms.CapturedRenderingState;
import net.minecraft.client.renderer.SubmitNodeCollection;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.feature.ItemFeatureRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemFeatureRenderer.class)
public class MixinItemFeatureRenderer {
	@Inject(method = "prepareSubmit", at = @At(value = "HEAD"))
	private void iris$set(ItemFeatureRenderer.Submit submit, CallbackInfo ci) {
		((ModelStorage) (Object) submit).iris$set();
	}

	@Inject(method = "prepareSubmit", at = @At("RETURN"))
	private void iris$clear(ItemFeatureRenderer.Submit submit, CallbackInfo ci) {
		CapturedRenderingState.INSTANCE.setCurrentRenderedItem(0);
		CapturedRenderingState.INSTANCE.setCurrentEntity(0);
		CapturedRenderingState.INSTANCE.setCurrentBlockEntity(0);
	}
}
