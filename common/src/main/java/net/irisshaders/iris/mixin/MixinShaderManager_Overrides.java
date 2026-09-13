package net.irisshaders.iris.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.pipeline.CompiledRenderPipeline;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.renderpearl.frontend.FrontendRenderPipeline;
import net.irisshaders.iris.pipeline.IrisPipelines;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RenderSystem.class)
public abstract class MixinShaderManager_Overrides {
	@Inject(method = "getCompiledPipelineNullable", at = @At("RETURN"))
	private static void iris$rememberPipeline(RenderPipeline source, CallbackInfoReturnable<CompiledRenderPipeline> cir) {
		if (cir.getReturnValue() instanceof FrontendRenderPipeline pipeline) {
			IrisPipelines.register(pipeline.backendRenderPipeline(), source);
		}
	}
}
