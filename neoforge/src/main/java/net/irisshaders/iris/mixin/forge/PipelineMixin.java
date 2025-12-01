package net.irisshaders.iris.mixin.forge;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.irisshaders.iris.pipeline.IrisPipelines;
import net.irisshaders.iris.platform.PipelineBuilderStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RenderPipeline.class)
public class PipelineMixin {
	@Inject(method = "toBuilder", at = @At("RETURN"))
	private void copyPipeline(CallbackInfoReturnable<RenderPipeline.Builder> cir) {
		((PipelineBuilderStorage) cir.getReturnValue()).copyPipelineShaderFrom((RenderPipeline) (Object) this);
	}
}
