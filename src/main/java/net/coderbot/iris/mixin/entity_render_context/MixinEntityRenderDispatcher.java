package net.coderbot.iris.mixin.entity_render_context;

import net.coderbot.iris.block_rendering.BlockRenderingSettings;
import net.coderbot.iris.fantastic.WrappingVertexConsumerProvider;
import net.coderbot.iris.layer.EntityRenderPhase;
import net.coderbot.iris.layer.OuterWrappedRenderLayer;
import net.coderbot.iris.shaderpack.IdMap;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
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
	private static final String RENDER =
			"render(Lnet/minecraft/entity/Entity;DDDFFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V";
	private static final String MATRIXSTACK_PUSH = "net/minecraft/client/util/math/MatrixStack.push ()V";
	private static final String MATRIXSTACK_POP = "net/minecraft/client/util/math/MatrixStack.pop ()V";
	private static final String CRASHREPORT_CREATE =
			"net/minecraft/util/crash/CrashReport.create (Ljava/lang/Throwable;Ljava/lang/String;)Lnet/minecraft/util/crash/CrashReport;";

	// Inject after MatrixStack#push to increase the chances that we won't be caught out by a poorly-positioned
	// cancellation in an inject.
	@Inject(method = RENDER, at = @At(value = "INVOKE", target = MATRIXSTACK_PUSH, shift = At.Shift.AFTER))
	private void iris$beginEntityRender(Entity entity, double x, double y, double z, float yaw, float tickDelta,
										MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light,
										CallbackInfo ci) {
		if (!(vertexConsumers instanceof WrappingVertexConsumerProvider)) {
			return;
		}

		Identifier entityId = Registry.ENTITY_TYPE.getId(entity.getType());

		IdMap idMap = BlockRenderingSettings.INSTANCE.getIdMap();

		if (idMap == null) {
			return;
		}

		int intId = idMap.getEntityIdMap().getOrDefault(entityId, -1);
		RenderPhase phase = EntityRenderPhase.forId(intId);

		((WrappingVertexConsumerProvider) vertexConsumers).pushWrappingFunction(layer ->
				new OuterWrappedRenderLayer("iris:is_entity", layer, phase));
	}

	// Inject before MatrixStack#pop so that our wrapper stack management operations naturally line up
	// with vanilla's MatrixStack management functions.
	@Inject(method = RENDER, at = @At(value = "INVOKE", target = MATRIXSTACK_POP))
	private void iris$endEntityRender(Entity entity, double x, double y, double z, float yaw, float tickDelta,
									  MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light,
									  CallbackInfo ci) {
		if (!(vertexConsumers instanceof WrappingVertexConsumerProvider)) {
			return;
		}

		((WrappingVertexConsumerProvider) vertexConsumers).popWrappingFunction();
	}

	@Inject(method = RENDER, at = @At(value = "INVOKE", target = CRASHREPORT_CREATE))
	private void iris$crashedEntityRender(Entity entity, double x, double y, double z, float yaw, float tickDelta,
									      MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light,
									      CallbackInfo ci) {
		if (!(vertexConsumers instanceof WrappingVertexConsumerProvider)) {
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
			((WrappingVertexConsumerProvider) vertexConsumers).popWrappingFunction();
		} catch (Exception e) {
			// oh well, we're gonna crash anyways.
		}
	}
}
