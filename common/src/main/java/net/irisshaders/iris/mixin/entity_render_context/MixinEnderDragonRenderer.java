package net.irisshaders.iris.mixin.entity_render_context;

import com.mojang.blaze3d.vertex.PoseStack;
import net.irisshaders.iris.shaderpack.materialmap.NamespacedId;
import net.irisshaders.iris.shaderpack.materialmap.WorldRenderingSettings;
import net.irisshaders.iris.uniforms.CapturedRenderingState;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EnderDragonRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EnderDragonRenderer.class)
public class MixinEnderDragonRenderer {
	@Unique
	private static final NamespacedId END_BEAM = new NamespacedId("minecraft", "end_crystal_beam");
	@Unique
	private static int previousE;

	@Inject(method = "renderCrystalBeams", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;pushPose()V"))
	private static void changeId(float f, float g, float h, float i, int j, PoseStack poseStack, MultiBufferSource multiBufferSource, int k, CallbackInfo ci) {
		if (WorldRenderingSettings.INSTANCE.getEntityIds() == null) return;

		previousE = CapturedRenderingState.INSTANCE.getCurrentRenderedEntity();
		CapturedRenderingState.INSTANCE.setCurrentEntity(WorldRenderingSettings.INSTANCE.getEntityIds().applyAsInt(END_BEAM));
	}

	@Inject(method = "renderCrystalBeams", at = @At(value = "RETURN"))
	private static void changeId2(CallbackInfo ci) {
		if (previousE != 0) {
			CapturedRenderingState.INSTANCE.setCurrentEntity(previousE);
			previousE = 0;
		}
	}
}
