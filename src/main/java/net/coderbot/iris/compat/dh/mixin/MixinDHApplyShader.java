package net.coderbot.iris.compat.dh.mixin;

import com.seibel.distanthorizons.core.render.renderer.shaders.DhApplyShader;
import com.seibel.distanthorizons.core.render.renderer.shaders.SSAOApplyShader;
import com.seibel.distanthorizons.core.wrapperInterfaces.minecraft.IMinecraftRenderWrapper;
import net.coderbot.iris.Iris;
import net.coderbot.iris.compat.dh.DHCompat;
import net.coderbot.iris.compat.dh.DHCompatInternal;
import net.coderbot.iris.pipeline.WorldRenderingPipeline;
import net.coderbot.iris.pipeline.newshader.NewWorldRenderingPipeline;
import net.irisshaders.iris.api.v0.IrisApi;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = {
	DhApplyShader.class,
	SSAOApplyShader.class
}, remap = false)
public class MixinDHApplyShader {
	//@Redirect(method = "onRender", at = @At(value = "INVOKE", target = "Lcom/seibel/distanthorizons/core/wrapperInterfaces/minecraft/IMinecraftRenderWrapper;getTargetFrameBuffer()I"))
	private int changeFB(IMinecraftRenderWrapper instance) {
		if (Iris.getPipelineManager().getPipelineNullable() instanceof NewWorldRenderingPipeline pipeline) {
			return pipeline.getDHCompat().getFramebuffer();
		} else {
			return instance.getTargetFrameBuffer();
		}
	}

	@Inject(method = "onRender", at = @At("HEAD"), cancellable = true)
	private void onRender2(CallbackInfo ci) {
		if (DHCompatInternal.INSTANCE.shouldOverride) ci.cancel();
	}
}
