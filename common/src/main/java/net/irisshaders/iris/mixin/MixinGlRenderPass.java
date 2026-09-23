package net.irisshaders.iris.mixin;

import com.mojang.renderpearl.api.textures.GpuSampler;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import com.mojang.renderpearl.frontend.FrontendRenderPass;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.pbr.TextureTracker;
import net.irisshaders.iris.pipeline.WorldRenderingPipeline;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FrontendRenderPass.class)
public class MixinGlRenderPass {
	@Inject(method = "setUniform(Ljava/lang/String;Lcom/mojang/renderpearl/api/textures/GpuTextureView;" + "Lcom/mojang/renderpearl/api/textures/GpuSampler;)V", at = @At("HEAD"))
	private void iris$bind(String name, GpuTextureView textureView, GpuSampler sampler, CallbackInfo ci) {
		if (name.equals("Sampler0") || name.equals("u_BlockTex") || name.equals("u_MainSampler")) {
			TextureTracker.INSTANCE.bindTexture(textureView == null ? null : textureView.texture());

			WorldRenderingPipeline irp = Iris.getPipelineManager().getPipelineNullable();

			if (textureView != null && irp != null) {
				irp.onSetAlbedoTex(textureView);
			}
		}
	}
}
