package net.irisshaders.iris.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.irisshaders.iris.Iris;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelReader;

@Environment(EnvType.CLIENT)
@Mixin(EntityRenderDispatcher.class)
public class MixinEntityRenderDispatcher {
	private static final String RENDER_SHADOW =
		"Lnet/minecraft/client/renderer/entity/EntityRenderDispatcher;renderShadow(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/world/entity/Entity;FFLnet/minecraft/world/level/LevelReader;F)V";

	@Inject(method = RENDER_SHADOW, at = @At("HEAD"), cancellable = true)
	private static void iris$maybeSuppressEntityShadow(PoseStack poseStack, MultiBufferSource bufferSource,
													   Entity entity, float opacity, float tickDelta, LevelReader level,
													   float radius, CallbackInfo ci) {
		if (Iris.getPipelineManager().getPipeline().shouldDisableVanillaEntityShadows()) {
			ci.cancel();
		}
	}
}
