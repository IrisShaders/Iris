package net.irisshaders.iris.mixin.forge;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.irisshaders.iris.pipeline.IrisPipelines;
import net.irisshaders.iris.platform.PipelineBuilderStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RenderPipeline.Builder.class)
public class PipelineBuilderMixin implements PipelineBuilderStorage {
	@Unique
	private RenderPipeline pipelineToCopy;

	@Override
	public void copyPipelineShaderFrom(RenderPipeline pipeline) {
		pipelineToCopy = pipeline;
	}

	@Inject(method = "build", at = @At("RETURN"))
	private void buildPipeline(CallbackInfoReturnable<RenderPipeline> cir) {
		if (pipelineToCopy != null) {
			IrisPipelines.copyPipeline(pipelineToCopy, cir.getReturnValue());
		}
	}
}
