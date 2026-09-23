package net.irisshaders.iris.mixin.entity_render_context;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.irisshaders.iris.mixinterface.ModelStorage;
import net.irisshaders.iris.uniforms.CapturedRenderingState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ModelFeatureRenderer.class)
public class MixinModelFeatureRenderer {
	@WrapMethod(method = "prepareModel")
	private <S> void iris$set(ModelFeatureRenderer.Submit<S> submit, Operation<Void> original) {
		((ModelStorage) (Object) submit).iris$set();
		original.call(submit);
		CapturedRenderingState.INSTANCE.setCurrentRenderedItem(0);
		CapturedRenderingState.INSTANCE.setCurrentEntity(0);
		CapturedRenderingState.INSTANCE.setCurrentBlockEntity(0);
	}
}
