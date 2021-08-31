package net.coderbot.iris.mixin;

import net.coderbot.iris.Iris;
import com.mojang.blaze3d.vertex.PoseStack;
import net.coderbot.iris.layer.EntityColorRenderState;
import net.coderbot.iris.layer.InnerWrappedRenderType;
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
	private MultiBufferSource iris$wrapProvider(MultiBufferSource provider, LivingEntity entity, float yaw, float tickDelta, PoseStack matrices, MultiBufferSource vertexConsumers, int light) {
		boolean hurt;
		if(Iris.isPhysicsModInstalled()) {
			hurt = entity.hurtTime > 0 && !entity.isDead();
		} else {
			hurt = entity.hurtTime > 0 || entity.deathTime > 0;
		}
		float whiteFlash = getAttackAnim(entity, tickDelta);

		if (hurt || whiteFlash > 0.0) {
			// TODO: Don't round the white flash?
			// This rounding kinda changes how creeper flashes work but it isn't particularly noticeable.
			// It avoids a big waste of memory with the current buffered entity rendering code creepers are exploding.
			EntityColorRenderState phase = new EntityColorRenderState(hurt, Math.round(whiteFlash));
			return layer -> provider.getBuffer(new InnerWrappedRenderType("iris_entity_color", layer, phase));
		} else {
			return provider;
		}
	}
}
