package net.coderbot.iris.mixin.entity_render_context;

import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.objects.Object2IntFunction;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.coderbot.batchedentityrendering.impl.Groupable;
import net.coderbot.iris.block_rendering.BlockRenderingSettings;
import net.coderbot.iris.fantastic.WrappingMultiBufferSource;
import net.coderbot.iris.layer.BlockEntityRenderStateShard;
import net.coderbot.iris.layer.EntityRenderStateShard;
import net.coderbot.iris.layer.OuterWrappedRenderType;
import net.coderbot.iris.shaderpack.materialmap.NamespacedId;
import net.coderbot.iris.uniforms.CapturedRenderingState;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.ZombieVillager;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Wraps entity rendering functions in order to create additional render layers
 * that provide context to shaders about what entity is currently being
 * rendered.
 */
@Mixin(EntityRenderDispatcher.class)
public abstract class MixinEntityRenderDispatcher {
	@Shadow
	public abstract <T extends Entity> EntityRenderer<? super T> getRenderer(T pEntityRenderDispatcher0);

	// Inject after MatrixStack#push since at this point we know that most cancellation checks have already passed.
	@ModifyVariable(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;pushPose()V", shift = At.Shift.AFTER),
		allow = 1, require = 1)
	private MultiBufferSource iris$beginEntityRender(MultiBufferSource bufferSource, Entity entity,
													 double pDouble1,
													 double pDouble2,
													 double pDouble3,
													 float pFloat4,
													 float pFloat5) {

		Vec3 lvVec314 = this.getRenderer(entity).getRenderOffset(entity, pFloat5);
		double lvDouble15 = pDouble1 + lvVec314.x();
		double lvDouble17 = pDouble2 + lvVec314.y();
		double lvDouble19 = pDouble3 + lvVec314.z();
		CapturedRenderingState.INSTANCE.velocityInfoEdit.pushPose();
		CapturedRenderingState.INSTANCE.velocityInfoEdit.translate(lvDouble15, lvDouble17, lvDouble19);

		if (!(bufferSource instanceof Groupable)) {
			// Fully batched entity rendering is not being used, do not use this wrapper!!!
			return bufferSource;
		}

		Object2IntFunction<NamespacedId> entityIds = BlockRenderingSettings.INSTANCE.getEntityIds();


		if (entityIds == null) {
			return bufferSource;
		}

		int intId;

		if (entity instanceof ZombieVillager zombie && zombie.isConverting() && BlockRenderingSettings.INSTANCE.hasVillagerConversionId()) {
			intId = entityIds.applyAsInt(new NamespacedId("minecraft", "zombie_villager_converting"));;
		} else {
			ResourceLocation entityId = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
			intId = entityIds.applyAsInt(new NamespacedId(entityId.getNamespace(), entityId.getPath()));
		}

		CapturedRenderingState.INSTANCE.setCurrentEntity(intId);
		CapturedRenderingState.INSTANCE.setUniqueEntityId(entity.getId());

		return type ->
			bufferSource.getBuffer(OuterWrappedRenderType.wrapExactlyOnce("iris:is_entity", type, EntityRenderStateShard.INSTANCE));
	}

	// Inject before MatrixStack#pop so that our wrapper stack management operations naturally line up
	// with vanilla's MatrixStack management functions.
	@Inject(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;popPose()V"))
	private void iris$endEntityRender(Entity entity, double x, double y, double z, float yaw, float tickDelta,
									  PoseStack poseStack, MultiBufferSource bufferSource, int light,
									  CallbackInfo ci) {
		CapturedRenderingState.INSTANCE.setCurrentEntity(0);
		CapturedRenderingState.INSTANCE.setUniqueEntityId(0);
		CapturedRenderingState.INSTANCE.velocityInfoEdit.popPose();
	}
}
