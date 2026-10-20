package net.irisshaders.iris.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.renderpearl.api.pipeline.CompiledRenderPipeline;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.renderpearl.api.pipeline.ShaderSource;
import com.mojang.renderpearl.frontend.FrontendRenderPipeline;
import com.mojang.renderpearl.frontend.shaders.PipelineBuilder;
import net.irisshaders.iris.mixinterface.FrontendRenderPipelineAccess;
import net.irisshaders.iris.pipeline.IrisPipelines;
import net.irisshaders.iris.vertices.ImmediateState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Mixin(PipelineBuilder.class)
public class MixinPipelineBuilder {
	@WrapMethod(method = "compilePipeline")
	private CompletableFuture<CompiledRenderPipeline.Pending> iris$useDeclaredVertexFormats(RenderPipeline pipeline,
		ShaderSource shaderSource, Executor executor, Operation<CompletableFuture<CompiledRenderPipeline.Pending>> original) {
		IrisPipelines.registerPipeline(pipeline);
		boolean previous = ImmediateState.skipExtension.get();
		ImmediateState.skipExtension.set(true);
		try {
			return original.call(pipeline, shaderSource, executor);
		} finally {
			ImmediateState.skipExtension.set(previous);
		}
	}

	@ModifyReturnValue(method = "compilePipeline", at = @At("RETURN"))
	private CompletableFuture<CompiledRenderPipeline.Pending> iris$finishWithDeclaredVertexFormats(
		CompletableFuture<CompiledRenderPipeline.Pending> original, RenderPipeline pipeline) {
		return original.thenApply(inner -> () -> {
			boolean previous = ImmediateState.skipExtension.get();
			ImmediateState.skipExtension.set(true);
			try {
				var compiled = inner.finishCompile();
				if (compiled instanceof FrontendRenderPipeline frontend) {
					((FrontendRenderPipelineAccess) (Object) frontend).iris$setSource(pipeline);
				}
				return compiled;
			} finally {
				ImmediateState.skipExtension.set(previous);
			}
		});
	}
}
