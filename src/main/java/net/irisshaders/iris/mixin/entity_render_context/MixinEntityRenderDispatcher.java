package net.irisshaders.iris.mixin.entity_render_context;

import com.mojang.blaze3d.vertex.PoseStack;
import net.irisshaders.iris.block_rendering.BlockRenderingSettings;
import net.irisshaders.iris.fantastic.WrappingMultiBufferSource;
import net.irisshaders.iris.layer.EntityRenderStateShard;
import net.irisshaders.iris.layer.OuterWrappedRenderType;
import net.irisshaders.iris.shaderpack.IdMap;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Wraps entity rendering functions in order to create additional render layers
 * that provide context to shaders about what entity is currently being
 * rendered.
 */
@Mixin(EntityRenderDispatcher.class)
public class MixinEntityRenderDispatcher {
	private static final String CRASHREPORT_CREATE =
			"Lnet/minecraft/world/entity/Entity;fillCrashReportCategory(Lnet/minecraft/CrashReportCategory;)V";

	// Inject after MatrixStack#push to increase the chances that we won't be caught out by a poorly-positioned
	// cancellation in an inject.
	@Inject(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;pushPose()V", shift = At.Shift.AFTER))
	private void iris$beginEntityRender(Entity entity, double x, double y, double z, float yaw, float tickDelta,
										PoseStack poseStack, MultiBufferSource bufferSource, int light,
										CallbackInfo ci) {
		if (!(bufferSource instanceof WrappingMultiBufferSource)) {
			return;
		}

		ResourceLocation entityId = Registry.ENTITY_TYPE.getKey(entity.getType());

		IdMap idMap = BlockRenderingSettings.INSTANCE.getIdMap();

		if (idMap == null) {
			return;
		}

		int intId = idMap.getEntityIdMap().getOrDefault(entityId, -1);
		RenderStateShard phase = EntityRenderStateShard.forId(intId);

		((WrappingMultiBufferSource) bufferSource).pushWrappingFunction(layer ->
				new OuterWrappedRenderType("iris:is_entity", layer, phase));
	}

	// Inject before MatrixStack#pop so that our wrapper stack management operations naturally line up
	// with vanilla's MatrixStack management functions.
	@Inject(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;popPose()V"))
	private void iris$endEntityRender(Entity entity, double x, double y, double z, float yaw, float tickDelta,
									  PoseStack poseStack, MultiBufferSource bufferSource, int light,
									  CallbackInfo ci) {
		if (!(bufferSource instanceof WrappingMultiBufferSource)) {
			return;
		}

		((WrappingMultiBufferSource) bufferSource).popWrappingFunction();
	}

	@Inject(method = "render", at = @At(value = "INVOKE", target = CRASHREPORT_CREATE))
	private void iris$crashedEntityRender(Entity entity, double x, double y, double z, float yaw, float tickDelta,
									      PoseStack poseStack, MultiBufferSource bufferSource, int light,
									      CallbackInfo ci) {
		if (!(bufferSource instanceof WrappingMultiBufferSource)) {
			return;
		}

		try {
			// Try to avoid leaving the wrapping stack in a bad state if we crash.
			// This will only be an issue with mods like NotEnoughCrashes that try
			// to act like nothing happened when a fatal error occurs.
			//
			// This could fail if we crash before MatrixStack#push, but this is mostly
			// a best-effort thing, it doesn't have to work perfectly. NEC will cause
			// weird chaos no matter what we do.
			((WrappingMultiBufferSource) bufferSource).popWrappingFunction();
		} catch (Exception e) {
			// oh well, we're gonna crash anyways.
		}
	}
}
