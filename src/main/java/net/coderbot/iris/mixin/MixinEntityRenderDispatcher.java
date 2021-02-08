package net.coderbot.iris.mixin;

import net.coderbot.iris.Iris;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.world.WorldView;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
@Mixin(EntityRenderDispatcher.class)
public class MixinEntityRenderDispatcher {
	private static final String RENDER_SHADOW =
		"renderShadow(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/entity/Entity;FFLnet/minecraft/world/WorldView;F)V";

	@Inject(method = RENDER_SHADOW, at = @At("HEAD"), cancellable = true)
	private static void iris$maybeSuppressEntityShadow(MatrixStack matrices, VertexConsumerProvider vertexConsumers,
													   Entity entity, float opacity, float tickDelta, WorldView world,
													   float radius, CallbackInfo ci) {
		if (Iris.getPipeline().shouldDisableVanillaEntityShadows()) {
			ci.cancel();
		}
	}
}
