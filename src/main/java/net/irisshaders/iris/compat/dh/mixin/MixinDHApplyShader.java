package net.irisshaders.iris.compat.dh.mixin;

import com.seibel.distanthorizons.core.render.renderer.shaders.DhApplyShader;
import com.seibel.distanthorizons.core.wrapperInterfaces.minecraft.IMinecraftRenderWrapper;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.pipeline.NewWorldRenderingPipeline;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = DhApplyShader.class, remap = false)
public class MixinDHApplyShader {
	@Redirect(method = "onRender", at = @At(value = "INVOKE", target = "Lcom/seibel/distanthorizons/core/wrapperInterfaces/minecraft/IMinecraftRenderWrapper;getTargetFrameBuffer()I"))
	private int changeFB(IMinecraftRenderWrapper instance) {
		if (Iris.getPipelineManager().getPipelineNullable() instanceof NewWorldRenderingPipeline pipeline) {
			return pipeline.getDHCompat().getFramebuffer();
		} else {
			return instance.getTargetFrameBuffer();
		}
	}
}
