package net.irisshaders.iris.mixin;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.pipeline.IrisRenderingPipeline;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Collections;
import java.util.Set;

@Mixin(LevelRenderer.class)
public class MixinLevelRenderer_SkipRendering {
	@Shadow
	@Final
	private ObjectArrayList<SectionRenderDispatcher.RenderSection> visibleSections;
	@Shadow
	@Final
	private Set<BlockEntity> globalBlockEntities;
	@Unique
	private static final ObjectArrayList<SectionRenderDispatcher.RenderSection> EMPTY_LIST = new ObjectArrayList<>();

	@WrapWithCondition(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;setupRender(Lnet/minecraft/client/Camera;Lnet/minecraft/client/renderer/culling/Frustum;ZZ)V"))
	private boolean skipSetupRender(LevelRenderer instance, Camera camera, Frustum frustum, boolean bl, boolean bl2) {
		if (Iris.getPipelineManager().getPipelineNullable() instanceof IrisRenderingPipeline pipeline) {
			return !pipeline.skipAllRendering();
		} else {
			return true;
		}
	}

	@WrapWithCondition(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;renderSectionLayer(Lnet/minecraft/client/renderer/RenderType;Lcom/mojang/blaze3d/vertex/PoseStack;DDDLorg/joml/Matrix4f;)V"))
	private boolean skipRenderChunks(LevelRenderer instance, RenderType renderType, PoseStack poseStack, double d, double e, double f, Matrix4f matrix4f) {
		if (Iris.getPipelineManager().getPipelineNullable() instanceof IrisRenderingPipeline pipeline) {
			return !pipeline.skipAllRendering();
		} else {
			return true;
		}
	}

	@WrapOperation(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;entitiesForRendering()Ljava/lang/Iterable;"))
	private Iterable<Entity> skipRenderEntities(ClientLevel instance, Operation<Iterable<Entity>> original) {
		if (Iris.getPipelineManager().getPipelineNullable() instanceof IrisRenderingPipeline pipeline && pipeline.skipAllRendering()) {
			return Collections.emptyList();
		} else {
			return original.call(instance);
		}
	}

	@WrapOperation(method = "renderLevel", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/LevelRenderer;visibleSections:Lit/unimi/dsi/fastutil/objects/ObjectArrayList;"))
	private ObjectArrayList<SectionRenderDispatcher.RenderSection> skipLocalBlockEntities(LevelRenderer instance, Operation<ObjectArrayList<SectionRenderDispatcher.RenderSection>> original) {
		if (Iris.getPipelineManager().getPipelineNullable() instanceof IrisRenderingPipeline pipeline && pipeline.skipAllRendering()) {
			return EMPTY_LIST;
		} else {
			return original.call(instance);
		}
	}

	@WrapOperation(method = "renderLevel", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/LevelRenderer;globalBlockEntities:Ljava/util/Set;"))
	private Set<BlockEntity> skipGlobalBlockEntities(LevelRenderer instance, Operation<Set<BlockEntity>> original) {
		if (Iris.getPipelineManager().getPipelineNullable() instanceof IrisRenderingPipeline pipeline && pipeline.skipAllRendering()) {
			return Collections.emptySet();
		} else {
			return original.call(instance);
		}
	}
}
