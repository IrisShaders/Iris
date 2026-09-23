package net.irisshaders.iris.mixin;

import com.mojang.renderpearl.backend.opengl.GlRenderPipeline;
import net.irisshaders.iris.opengl.GlPipelineState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GlRenderPipeline.class)
public class MixinGlRenderPipeline {
	@Inject(method = "bind", at = @At("HEAD"))
	private void iris$restoreIndexedBlend(CallbackInfo ci) {
		GlPipelineState.beforeVanillaPipeline();
	}
}
