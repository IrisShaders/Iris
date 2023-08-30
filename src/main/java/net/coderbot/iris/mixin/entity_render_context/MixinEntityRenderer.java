package net.coderbot.iris.mixin.entity_render_context;

import com.mojang.blaze3d.vertex.PoseStack;
import net.coderbot.iris.uniforms.CapturedRenderingState;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public class MixinEntityRenderer {
	@Shadow
	@Final
	protected EntityRenderDispatcher entityRenderDispatcher;

	@Inject(method = "renderNameTag", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getNameTagOffsetY()F"))
	private void iris$editVelocityInfo(Entity pEntityRenderer0, Component pComponent1, PoseStack pPoseStack2, MultiBufferSource pMultiBufferSource3, int pInt4, CallbackInfo ci) {
		CapturedRenderingState.INSTANCE.velocityInfoEdit.translate(0.0F, pEntityRenderer0.getNameTagOffsetY(), 0.0F);
		CapturedRenderingState.INSTANCE.velocityInfoEdit.mulPose(this.entityRenderDispatcher.cameraOrientation());
		CapturedRenderingState.INSTANCE.velocityInfoEdit.scale(-0.025F, -0.025F, 0.025F);
	}
}
