package net.irisshaders.iris.mixin.entity_render_context;

import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.objects.Object2IntFunction;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.irisshaders.iris.layer.BufferSourceWrapper;
import net.irisshaders.iris.layer.EntityRenderStateShard;
import net.irisshaders.iris.layer.OuterWrappedRenderType;
import net.irisshaders.iris.shaderpack.materialmap.NamespacedId;
import net.irisshaders.iris.shaderpack.materialmap.WorldRenderingSettings;
import net.irisshaders.iris.uniforms.CapturedRenderingState;
import net.irisshaders.iris.vertices.ImmediateState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.ZombieVillagerRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.ZombieVillager;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
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
	@Unique
	private static final NamespacedId CURRENT_PLAYER = new NamespacedId("minecraft", "current_player");

	@Unique
	private static final NamespacedId CONVERTING_VILLAGER = new NamespacedId("minecraft", "zombie_villager_converting");

	@Unique
	private static final Object2ObjectMap<EntityType<?>, NamespacedId> ENTITY_IDS = new Object2ObjectOpenHashMap<>();

	// Inject after MatrixStack#push since at this point we know that most cancellation checks have already passed.
	@Inject(method = "submit", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;pushPose()V", shift = At.Shift.AFTER))
	private <E extends Entity, S extends EntityRenderState> void iris$beginEntityRender(S entity, CameraRenderState cameraRenderState, double d, double e, double f, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CallbackInfo ci) {
		Object2IntFunction<NamespacedId> entityIds = WorldRenderingSettings.INSTANCE.getEntityIds();

		if (entityIds == null || !ImmediateState.isRenderingLevel) {
			return;
		}

		int intId;

		// TODO: Add special types

		if (entity instanceof ZombieVillagerRenderState zombie && zombie.isConverting && WorldRenderingSettings.INSTANCE.hasVillagerConversionId()) {
			intId = entityIds.applyAsInt(CONVERTING_VILLAGER);
		} else if (entity instanceof AvatarRenderState ars && Minecraft.getInstance().getCameraEntity() instanceof AbstractClientPlayer acs && acs.getId() == ars.id) {
			if (entityIds.containsKey(CURRENT_PLAYER)) {
				intId = entityIds.getInt(CURRENT_PLAYER);
			} else {
				intId = entityIds.applyAsInt(ENTITY_IDS.computeIfAbsent(entity.entityType, k -> {
					ResourceLocation entityId = BuiltInRegistries.ENTITY_TYPE.getKey(entity.entityType);
					return new NamespacedId(entityId.getNamespace(), entityId.getPath());
				}));
			}
		} else {
			intId = entityIds.applyAsInt(ENTITY_IDS.computeIfAbsent(entity.entityType, k -> {
				ResourceLocation entityId = BuiltInRegistries.ENTITY_TYPE.getKey(entity.entityType);
				return new NamespacedId(entityId.getNamespace(), entityId.getPath());
			}));
		}

		CapturedRenderingState.INSTANCE.setCurrentEntity(intId);
	}

	// Inject before MatrixStack#pop so that our wrapper stack management operations naturally line up
	// with vanilla's MatrixStack management functions.
	@Inject(method = "submit", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;popPose()V"))
	private<E extends Entity, S extends EntityRenderState> void iris$endEntityRender(S entityRenderState, CameraRenderState cameraRenderState, double d, double e, double f, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CallbackInfo ci) {
		CapturedRenderingState.INSTANCE.setCurrentEntity(0);
		CapturedRenderingState.INSTANCE.setCurrentRenderedItem(0);
	}
}
