package net.coderbot.iris.mixin.entity_render_context;

import it.unimi.dsi.fastutil.objects.Object2IntFunction;
import net.coderbot.batchedentityrendering.impl.Groupable;
import net.coderbot.iris.block_rendering.BlockRenderingSettings;
import net.coderbot.iris.layer.EntityRenderStateShard;
import net.coderbot.iris.layer.OuterWrappedRenderType;
import net.coderbot.iris.shaderpack.materialmap.NamespacedId;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Wraps entity rendering functions in order to create additional render layers
 * that provide context to shaders about what entity is currently being
 * rendered.
 */
@Mixin(EntityRenderDispatcher.class)
public class MixinEntityRenderDispatcher {
	// Inject after MatrixStack#push since at this point we know that most cancellation checks have already passed.
	@ModifyVariable(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;pushPose()V", shift = At.Shift.AFTER),
		allow = 1, require = 1)
	private MultiBufferSource iris$beginEntityRender(MultiBufferSource bufferSource, Entity entity) {
		if (!(bufferSource instanceof Groupable)) {
			// Fully batched entity rendering is not being used, do not use this wrapper!!!
			return bufferSource;
		}

		ResourceLocation entityId = Registry.ENTITY_TYPE.getKey(entity.getType());

		Object2IntFunction<NamespacedId> entityIds = BlockRenderingSettings.INSTANCE.getEntityIds();

		if (entityIds == null) {
			return bufferSource;
		}

		int intId = entityIds.applyAsInt(new NamespacedId(entityId.getNamespace(), entityId.getPath()));
		RenderStateShard phase = EntityRenderStateShard.forId(intId);

		return type ->
				bufferSource.getBuffer(OuterWrappedRenderType.wrapExactlyOnce("iris:is_entity", type, phase));
	}
}
