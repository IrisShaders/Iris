package net.irisshaders.iris.mixin;

import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.renderpearl.api.vertex.VertexFormat;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.vertices.ImmediateState;
import net.irisshaders.iris.vertices.IrisVertexFormats;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(RenderPipeline.class)
public class MixinRenderPipeline {
	@Inject(method = "getVertexFormatBinding", at = @At("RETURN"), cancellable = true)
	private void iris$change(CallbackInfoReturnable<VertexFormat> cir) {
		if (Iris.isPackInUseQuick() && Thread.currentThread().getName()
			.contains("Render") && ImmediateState.isRenderingLevel && !ImmediateState.skipExtension.get()) {
			cir.setReturnValue(IrisVertexFormats.extend((RenderPipeline) (Object) this, cir.getReturnValue()));
		}
	}

	@Inject(method = "getVertexFormatBindings", at = @At("RETURN"), cancellable = true)
	private void iris$change2(CallbackInfoReturnable<List<VertexFormat>> cir) {
		if (Iris.isPackInUseQuick() && Thread.currentThread().getName()
			.contains("Render") && ImmediateState.isRenderingLevel && !ImmediateState.skipExtension.get()) {
			cir.setReturnValue(IrisVertexFormats.getExtendedFormats((RenderPipeline) (Object) this, cir.getReturnValue()));
		}
	}
}
