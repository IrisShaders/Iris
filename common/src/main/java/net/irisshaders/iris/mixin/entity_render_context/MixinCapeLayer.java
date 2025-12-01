package net.irisshaders.iris.mixin.entity_render_context;

import com.mojang.blaze3d.vertex.PoseStack;
import net.irisshaders.iris.shaderpack.materialmap.NamespacedId;
import net.irisshaders.iris.shaderpack.materialmap.WorldRenderingSettings;
import net.irisshaders.iris.uniforms.CapturedRenderingState;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.CapeLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CapeLayer.class)
public class MixinCapeLayer {
	@Unique
	private static final NamespacedId CAPE_LOCATION = new NamespacedId("minecraft", "player_cape");

	@Inject(method = "submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/client/renderer/entity/state/AvatarRenderState;FF)V", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;pushPose()V"))
	private void changeId(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int i, AvatarRenderState playerRenderState, float f, float g, CallbackInfo ci) {
		if (WorldRenderingSettings.INSTANCE.getItemIds() == null) return;

		CapturedRenderingState.INSTANCE.setCurrentRenderedItem(WorldRenderingSettings.INSTANCE.getItemIds().applyAsInt(CAPE_LOCATION));
	}

	@Inject(method = "submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/client/renderer/entity/state/AvatarRenderState;FF)V", at = @At(value = "RETURN"))
	private void changeId2(CallbackInfo ci) {
		CapturedRenderingState.INSTANCE.setCurrentRenderedItem(0);
	}
}
