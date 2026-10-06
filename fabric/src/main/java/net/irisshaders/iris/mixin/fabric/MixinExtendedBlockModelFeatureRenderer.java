package net.irisshaders.iris.mixin.fabric;

import com.llamalad7.mixinextras.sugar.Local;
import net.caffeinemc.mods.sodium.client.render.frapi.render.ExtendedBlockModelFeatureRenderer;
import net.fabricmc.fabric.api.client.renderer.v1.render.submit.ExtendedBlockModelSubmit;
import net.irisshaders.iris.mixinterface.ModelStorage;
import net.irisshaders.iris.uniforms.CapturedRenderingState;
import net.minecraft.client.renderer.feature.FeatureFrameContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ExtendedBlockModelFeatureRenderer.class)
public class MixinExtendedBlockModelFeatureRenderer {
	@Inject(method = "buildGroup", at = @At(value = "INVOKE", target = "Lnet/caffeinemc/mods/sodium/client/render/frapi/render/ExtendedBlockModelFeatureRenderer$BufferCache;prepare(Ljava/util/function/Function;Lcom/mojang/blaze3d/vertex/PoseStack$Pose;)V"))
	private void iris$set(FeatureFrameContext context, List<ExtendedBlockModelSubmit> submits, CallbackInfo ci, @Local ExtendedBlockModelSubmit submit) {
		((ModelStorage) (Object) submit).iris$set();
	}

	@Inject(method = "buildGroup", at = @At("RETURN"))
	private void iris$unset(FeatureFrameContext context, List<ExtendedBlockModelSubmit> submits, CallbackInfo ci) {
		CapturedRenderingState.INSTANCE.setCurrentRenderedItem(0);
		CapturedRenderingState.INSTANCE.setCurrentEntity(0);
		CapturedRenderingState.INSTANCE.setCurrentBlockEntity(0);
	}
}
