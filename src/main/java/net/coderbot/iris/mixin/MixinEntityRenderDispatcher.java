package net.coderbot.iris.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.coderbot.iris.Iris;
import net.coderbot.iris.pipeline.WorldRenderingPipeline;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderDispatcher.class)
public class MixinEntityRenderDispatcher {
	private static final String RENDER_SHADOW =
		"renderShadow(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/world/entity/Entity;FFLnet/minecraft/world/level/LevelReader;F)V";
	private static final String RENDER_BLOCK_SHADOW =
		"renderBlockShadow(Lcom/mojang/blaze3d/vertex/PoseStack$Pose;Lcom/mojang/blaze3d/vertex/VertexConsumer;Lnet/minecraft/world/level/LevelReader;Lnet/minecraft/core/BlockPos;DDDFF)V";

	@Inject(method = RENDER_SHADOW, at = @At("HEAD"), cancellable = true)
	private static void iris$maybeSuppressEntityShadow(PoseStack poseStack, MultiBufferSource bufferSource,
													   Entity entity, float opacity, float tickDelta, LevelReader level,
													   float radius, CallbackInfo ci) {
		iris$maybeSuppressShadow(ci);
	}

	// The underlying method called by renderShadow.
	@Inject(method = RENDER_BLOCK_SHADOW, at = @At("HEAD"), cancellable = true)
	private static void renderBlockShadow(PoseStack.Pose pose, VertexConsumer vc, LevelReader level, BlockPos pos,
										  double d, double e, double f, float g, float h, CallbackInfo ci) {
		iris$maybeSuppressShadow(ci);
	}

	// First Person Model by tr7zw compatibility, this is a method added by First Person Model:
	// https://github.com/tr7zw/FirstPersonModel/blob/172ab05368832df82e34ca9f9b06814672f69f59/FPShared/src/main/java/dev/tr7zw/firstperson/mixins/RenderDispatcherMixin.java#L68
	// The renderBlockShadow injection will handle this, but it's easier to suppress it before all of the other calculations.
	@Inject(method = "renderOffsetShadow", at = @At("HEAD"), cancellable = true, require = 0, remap=false)
	private static void iris$maybeSuppressEntityShadow(PoseStack poseStack, MultiBufferSource bufferSource,
													   Entity entity, float opacity, float tickDelta, LevelReader level,
													   float radius, Vec3 offset, CallbackInfo ci) {
		iris$maybeSuppressShadow(ci);
	}

	@Unique
	private static void iris$maybeSuppressShadow(CallbackInfo ci) {
		WorldRenderingPipeline pipeline = Iris.getPipelineManager().getPipelineNullable();

		if (pipeline != null && pipeline.shouldDisableVanillaEntityShadows()) {
			ci.cancel();
		}
	}
}
