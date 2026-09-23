package net.irisshaders.iris.mixin.entity_render_context;

import com.llamalad7.mixinextras.sugar.Local;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.mixinterface.ModelStorage;
import net.irisshaders.iris.uniforms.CapturedRenderingState;
import net.irisshaders.iris.vertices.ImmediateState;
import net.minecraft.client.renderer.feature.FeatureFrameContext;
import net.minecraft.client.renderer.feature.TextFeatureRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(TextFeatureRenderer.class)
public class MixinTextFeatureRenderer {
	@Unique
	private boolean hasBE = false;

	@Inject(method = "buildGroup", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/feature/TextFeatureRenderer$Submit;pose()Lorg/joml/Matrix4fc;", ordinal = 0))
	private void iris$set(FeatureFrameContext context, List<TextFeatureRenderer.Submit> submits, CallbackInfo ci,
		@Local TextFeatureRenderer.Submit modelSubmit) {
		((ModelStorage) (Object) modelSubmit).iris$set();
		if (((ModelStorage) (Object) modelSubmit).iris$wasBE()) {
			hasBE = true;
			ImmediateState.isRenderingBEs = Iris.isPackInUseQuick();
		} else if (hasBE) {
			hasBE = false;
			ImmediateState.isRenderingBEs = false;
		}
	}

	@Inject(method = "buildGroup", at = @At("RETURN"))
	private void iris$clear(FeatureFrameContext context, List<TextFeatureRenderer.Submit> submits, CallbackInfo ci) {
		CapturedRenderingState.INSTANCE.setCurrentRenderedItem(0);
		CapturedRenderingState.INSTANCE.setCurrentEntity(0);
		CapturedRenderingState.INSTANCE.setCurrentBlockEntity(0);
		hasBE = false;
		ImmediateState.isRenderingBEs = false;
	}
}
