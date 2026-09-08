package net.irisshaders.iris.mixin;

import com.mojang.renderpearl.api.textures.GpuSampler;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import com.mojang.renderpearl.backend.opengl.GlRenderPass;
import com.mojang.renderpearl.frontend.FrontendRenderPass;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.pipeline.IrisRenderingPipeline;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FrontendRenderPass.class)
public class MixinGlRenderPass {
	@Inject(method = "setUniform(Ljava/lang/String;Lcom/mojang/renderpearl/api/textures/GpuTextureView;Lcom/mojang/renderpearl/api/textures/GpuSampler;)V", at = @At("HEAD"))
	private void iris$bind(String name, GpuTextureView textureView, GpuSampler sampler, CallbackInfo ci) {
		if (name.equals("Sampler0") || name.equals("u_BlockTex")) {
			if (textureView != null && Iris.getPipelineManager().getPipelineNullable() instanceof IrisRenderingPipeline irp) {
				irp.onSetAlbedoTex(textureView);
			}
		}
	}
}
