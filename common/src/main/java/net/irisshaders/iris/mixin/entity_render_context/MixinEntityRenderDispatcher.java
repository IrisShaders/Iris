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
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
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
import org.spongepowered.asm.mixin.injection.ModifyVariable;
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
	@ModifyVariable(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;pushPose()V", shift = At.Shift.AFTER),
		allow = 1, require = 1, argsOnly = true)
	private MultiBufferSource iris$beginEntityRender(MultiBufferSource bufferSource, Entity entity) {
		Object2IntFunction<NamespacedId> entityIds = WorldRenderingSettings.INSTANCE.getEntityIds();

		if (entityIds == null || !ImmediateState.isRenderingLevel) {
			return bufferSource;
		}

		int intId;

		if (entity instanceof ZombieVillager zombie && zombie.isConverting() && WorldRenderingSettings.INSTANCE.hasVillagerConversionId()) {
			intId = entityIds.applyAsInt(CONVERTING_VILLAGER);
		} else if (entity instanceof Player && Minecraft.getInstance().getCameraEntity() == entity) {
			if (entityIds.containsKey(CURRENT_PLAYER)) {
				intId = entityIds.getInt(CURRENT_PLAYER);
			} else {
				intId = entityIds.applyAsInt(ENTITY_IDS.computeIfAbsent(entity.getType(), k -> {
					ResourceLocation entityId = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
					return new NamespacedId(entityId.getNamespace(), entityId.getPath());
				}));
			}
		} else {
			intId = entityIds.applyAsInt(ENTITY_IDS.computeIfAbsent(entity.getType(), k -> {
				ResourceLocation entityId = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
				return new NamespacedId(entityId.getNamespace(), entityId.getPath());
			}));
		}

		CapturedRenderingState.INSTANCE.setCurrentEntity(intId);

		return new BufferSourceWrapper(bufferSource, (renderType) -> OuterWrappedRenderType.wrapExactlyOnce("iris:entity", renderType, EntityRenderStateShard.INSTANCE));
	}

	// Inject before MatrixStack#pop so that our wrapper stack management operations naturally line up
	// with vanilla's MatrixStack management functions.
	@Inject(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;popPose()V"))
	private void iris$endEntityRender(Entity entity, double x, double y, double z, float yaw, float tickDelta,
									  PoseStack poseStack, MultiBufferSource bufferSource, int light,
									  CallbackInfo ci) {
		CapturedRenderingState.INSTANCE.setCurrentEntity(0);
		CapturedRenderingState.INSTANCE.setCurrentRenderedItem(0);
	}
}
