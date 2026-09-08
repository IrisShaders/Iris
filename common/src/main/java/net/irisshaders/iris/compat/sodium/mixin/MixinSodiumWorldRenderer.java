package net.irisshaders.iris.compat.sodium.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.caffeinemc.mods.sodium.client.render.SodiumWorldRenderer;
import net.caffeinemc.mods.sodium.client.render.chunk.ChunkRenderMatrices;
import net.caffeinemc.mods.sodium.client.render.chunk.RenderSectionManager;
import net.caffeinemc.mods.sodium.client.render.chunk.UniformBufferManager;
import net.caffeinemc.mods.sodium.client.util.FogParameters;
import net.irisshaders.iris.mixinterface.ShadowRenderListAccess;
import net.irisshaders.iris.mixin.LevelRendererAccessor;
import net.irisshaders.iris.shadows.ShadowRenderer;
import net.irisshaders.iris.shadows.ShadowRenderingState;
import net.irisshaders.iris.uniforms.CapturedRenderingState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.server.level.BlockDestructionProgress;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.joml.Matrix4f;
import org.joml.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.SortedSet;

@Mixin(SodiumWorldRenderer.class)
public class MixinSodiumWorldRenderer implements ShadowRenderListAccess {
	@Shadow(remap = false)
	private RenderSectionManager renderSectionManager;

	@Shadow(remap = false)
	private Vector3d lastCameraPos;

	@Shadow(remap = false)
	private double lastCameraPitch;

	@Shadow(remap = false)
	private double lastCameraYaw;

	@Shadow(remap = false)
	private FogParameters lastFogParameters;

	@Shadow(remap = false)
	private Matrix4f cullMatrix;

	@Shadow
	private UniformBufferManager uniformBufferManager;
	@Unique
	private float lastSunAngle;

	@Unique
	private boolean iris$shadowScopeActive;

	@Unique
	private Vector3d iris$regularLastCameraPos;

	@Unique
	private double iris$regularLastCameraPitch;

	@Unique
	private double iris$regularLastCameraYaw;

	@Unique
	private FogParameters iris$regularLastFogParameters;

	@Unique
	private Matrix4f iris$regularCullMatrix;

	@Unique
	private Vector3d iris$shadowLastCameraPos;

	@Unique
	private double iris$shadowLastCameraPitch;

	@Unique
	private double iris$shadowLastCameraYaw;

	@Unique
	private FogParameters iris$shadowLastFogParameters = FogParameters.NONE;

	@Unique
	private Matrix4f iris$shadowCullMatrix;

	@Override
	public void iris$beginShadowRenderListScope() {
		if (!this.iris$shadowScopeActive) {
			this.iris$regularLastCameraPos = this.lastCameraPos;
			this.iris$regularLastCameraPitch = this.lastCameraPitch;
			this.iris$regularLastCameraYaw = this.lastCameraYaw;
			this.iris$regularLastFogParameters = this.lastFogParameters;
			this.iris$regularCullMatrix = this.cullMatrix;
			this.iris$shadowScopeActive = true;

			this.lastCameraPos = this.iris$shadowLastCameraPos;
			this.lastCameraPitch = this.iris$shadowLastCameraPitch;
			this.lastCameraYaw = this.iris$shadowLastCameraYaw;
			this.lastFogParameters = this.iris$shadowLastFogParameters == null ? FogParameters.NONE : this.iris$shadowLastFogParameters;
			this.cullMatrix = this.iris$shadowCullMatrix;
		}

		if (this.renderSectionManager instanceof ShadowRenderListAccess shadowRenderListAccess) {
			shadowRenderListAccess.iris$beginShadowRenderListScope();
		}

		((ShadowRenderListAccess) this.uniformBufferManager).iris$beginShadowRenderListScope();;
	}

	@Override
	public void iris$endShadowRenderListScope() {
		if (this.renderSectionManager instanceof ShadowRenderListAccess shadowRenderListAccess) {
			shadowRenderListAccess.iris$endShadowRenderListScope();
		}

		((ShadowRenderListAccess) this.uniformBufferManager).iris$endShadowRenderListScope();;

		if (this.iris$shadowScopeActive) {
			this.iris$shadowLastCameraPos = this.lastCameraPos;
			this.iris$shadowLastCameraPitch = this.lastCameraPitch;
			this.iris$shadowLastCameraYaw = this.lastCameraYaw;
			this.iris$shadowLastFogParameters = this.lastFogParameters;
			this.iris$shadowCullMatrix = this.cullMatrix;

			this.lastCameraPos = this.iris$regularLastCameraPos;
			this.lastCameraPitch = this.iris$regularLastCameraPitch;
			this.lastCameraYaw = this.iris$regularLastCameraYaw;
			this.lastFogParameters = this.iris$regularLastFogParameters == null ? FogParameters.NONE : this.iris$regularLastFogParameters;
			this.cullMatrix = this.iris$regularCullMatrix;

			this.iris$regularLastCameraPos = null;
			this.iris$regularLastFogParameters = null;
			this.iris$regularCullMatrix = null;
			this.iris$shadowScopeActive = false;
		}
	}

	@Inject(method = "scheduleTerrainUpdate", at = @At("HEAD"), cancellable = true, remap = false)
	private void iris$skipShadowTerrainUpdate(CallbackInfo ci) {
		if (ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
			ci.cancel();
		}
	}


	@Redirect(method = "setupTerrain", remap = false,
		at = @At(value = "INVOKE",
			target = "Lnet/caffeinemc/mods/sodium/client/render/chunk/RenderSectionManager;needsUpdate()Z", ordinal = 0,
			remap = false))
	private boolean iris$forceChunkGraphRebuildInShadowPass(RenderSectionManager instance) {
		if (ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
			float sunAngle = Minecraft.getInstance().gameRenderer.mainCamera().attributeProbe().getValue(EnvironmentAttributes.SUN_ANGLE, CapturedRenderingState.INSTANCE.getTickDelta());
			if (lastSunAngle != sunAngle) {
				lastSunAngle = sunAngle;
				return true;
			}
		}

		return instance.needsUpdate();
	}

	@WrapOperation(method = "setupTerrain",
		at = @At(value = "INVOKE",
			target = "Lnet/caffeinemc/mods/sodium/client/render/chunk/RenderSectionManager;needsUpdate()Z"))
	private boolean iris$forceEndGraphRebuild(RenderSectionManager instance, Operation<Boolean> original) {
		if (ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
			// TODO: Detect when the sun/moon isn't moving
			return false;
		} else {
			return original.call(instance);
		}
	}

	@Inject(method = "isEntityVisible", at = @At("HEAD"), cancellable = true)
	private <T extends Entity, S extends EntityRenderState> void iris$skipEntityCheck(EntityRenderer<T, S> renderer, Entity entity, float partialTicks, CallbackInfoReturnable<Boolean> cir) {
		if (ShadowRenderingState.areShadowsCurrentlyBeingRendered()) cir.setReturnValue(true);
	}
}
