package net.coderbot.iris.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.coderbot.batchedentityrendering.impl.Groupable;
import net.coderbot.iris.Iris;
import net.coderbot.iris.layer.EntityColorRenderStateShard;
import net.coderbot.iris.layer.EntityColorVertexConsumerProvider;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LivingEntityRenderer.class)
public abstract class MixinLivingEntityRenderer {
	@Shadow
	abstract float getAttackAnim(LivingEntity entity, float tickDelta);

	@ModifyVariable(method = "render", at = @At("HEAD"))
	private MultiBufferSource iris$wrapProvider(MultiBufferSource provider, LivingEntity entity, float yaw, float tickDelta, PoseStack pose, MultiBufferSource bufferSource, int light) {
		if (!(provider instanceof Groupable)) {
			// Entity color is not supported in this context, no buffering available.
			return provider;
		}

		boolean hurt;
		if(Iris.isPhysicsModInstalled()) {
			hurt = entity.hurtTime > 0 && !entity.isDeadOrDying();
		} else {
			hurt = entity.hurtTime > 0 || entity.deathTime > 0;
		}
		float whiteFlash = getAttackAnim(entity, tickDelta);

		if (hurt || whiteFlash > 0.0) {
			EntityColorRenderStateShard phase = new EntityColorRenderStateShard(hurt, whiteFlash);
			return new EntityColorVertexConsumerProvider(provider, phase);
		} else {
			return provider;
		}
	}
}
