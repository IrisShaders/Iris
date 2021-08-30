package net.coderbot.iris.mixin;

import net.coderbot.iris.Iris;
import net.coderbot.iris.layer.EntityColorRenderPhase;
import net.coderbot.iris.layer.EntityColorWrappedRenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LivingEntityRenderer.class)
public abstract class MixinLivingEntityRenderer {
	@Shadow
	abstract float getAnimationCounter(LivingEntity entity, float tickDelta);

	@ModifyVariable(method = "render", at = @At("HEAD"))
	private VertexConsumerProvider iris$wrapProvider(VertexConsumerProvider provider, LivingEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
		boolean hurt;
		if(Iris.isPhysicsModInstalled()) {
			hurt = entity.hurtTime > 0 && !entity.isDead();
		} else {
			hurt = entity.hurtTime > 0 || entity.deathTime > 0;
		}
		float whiteFlash = getAnimationCounter(entity, tickDelta);

		if (hurt || whiteFlash > 0.0) {
			// TODO: Don't round the white flash?
			// This rounding kinda changes how creeper flashes work but it isn't particularly noticeable.
			// It avoids a big waste of memory with the current buffered entity rendering code creepers are exploding.
			EntityColorRenderPhase phase = new EntityColorRenderPhase(hurt, Math.round(whiteFlash));
			return layer -> provider.getBuffer(new EntityColorWrappedRenderLayer("iris_entity_color", layer, phase));
		} else {
			return provider;
		}
	}
}
