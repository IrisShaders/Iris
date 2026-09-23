package net.irisshaders.iris.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.pipeline.CompiledRenderPipeline;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.renderpearl.frontend.FrontendRenderPipeline;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.pipeline.IrisPipelines;
import net.irisshaders.iris.pipeline.IrisRenderingPipeline;
import net.irisshaders.iris.vertices.ImmediateState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RenderSystem.class)
public abstract class MixinShaderManager_Overrides {
	@Inject(method = "getCompiledPipelineNullable", at = @At("RETURN"), cancellable = true)
	private static void iris$overridePipeline(RenderPipeline source, CallbackInfoReturnable<CompiledRenderPipeline> cir) {
		if (!(cir.getReturnValue() instanceof FrontendRenderPipeline original) || !(Iris.getPipelineManager()
			.getPipelineNullable() instanceof IrisRenderingPipeline iris) || !iris.shouldOverrideShaders() || ImmediateState.bypass || ImmediateState.temporarilyIgnorePass) {
			return;
		}

		var key = IrisPipelines.getPipeline(iris, source);
		var shader = key == null ? null : iris.getShaderMap().getShader(key);

		if (shader != null) {
			cir.setReturnValue(shader.override(source, original));
		}
	}
}
