package net.irisshaders.iris.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import net.irisshaders.iris.Iris;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.chunk.ChunkSectionsToRender;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * This mixin makes the effects of view bobbing and nausea apply to the model view matrix, not the projection matrix.
 * <p>
 * Applying these effects to the projection matrix causes severe issues with most shaderpacks. As it turns out, OptiFine
 * applies these effects to the modelview matrix. As such, we must do the same to properly run shaderpacks.
 * <p>
 * This mixin makes use of the matrix stack in order to make these changes without more invasive changes.
 */
@Mixin(GameRenderer.class)
public abstract class MixinModelViewBobbing {
	@Shadow
	@Final
	Minecraft minecraft;

	@Shadow
	@Final
	private Camera mainCamera;
	@Unique
	private Matrix4fc bobbingEffectsModel;
	@Unique
	private boolean areShadersOn;

	@Unique
	private Matrix4fc bobStack;

	@Shadow
	private float spinningEffectTime;

	@Shadow
	private float spinningEffectSpeed;

	@Inject(method = "renderLevel", at = @At("HEAD"))
	private void iris$saveShadersOn(DeltaTracker deltaTracker, CallbackInfo ci) {
		areShadersOn = Iris.isPackInUseQuick();
	}

	@WrapOperation(method = "renderLevel", at = @At(value = "INVOKE", target = "Lorg/joml/Matrix4f;mul(Lorg/joml/Matrix4fc;)Lorg/joml/Matrix4f;"))
	private Matrix4f iris$applyBobbingToModelView(Matrix4f instance, Matrix4fc right, Operation<Matrix4f> original, @Local(name = "cameraState") CameraRenderState cameraRenderState) {
		if (!areShadersOn) {
			return original.call(instance, right);
		}

		this.bobStack = new Matrix4f(right);
		return instance;
	}

	@WrapOperation(method = "renderLevel", at = @At(value = "INVOKE", target = "Lorg/joml/Matrix4f;rotate(FLorg/joml/Vector3fc;)Lorg/joml/Matrix4f;"))
	private Matrix4f iris$applySpinningRotate(Matrix4f instance, float angle, Vector3fc axis, Operation<Matrix4f> original) {
		if (!areShadersOn) {
			return original.call(instance, angle, axis);
		}

		((Matrix4f) bobStack).rotate(angle, axis);
		return instance;
	}

	@WrapOperation(method = "renderLevel", at = @At(value = "INVOKE", target = "Lorg/joml/Matrix4f;scale(FFF)Lorg/joml/Matrix4f;"))
	private Matrix4f iris$applySpinningScale(Matrix4f instance, float x, float y, float z, Operation<Matrix4f> original) {
		if (!areShadersOn) {
			return original.call(instance, x, y, z);
		}
		
		((Matrix4f) bobStack).scale(x, y, z);
		return instance;
	}

	@WrapOperation(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;renderLevel(Lcom/mojang/blaze3d/resource/GraphicsResourceAllocator;Lnet/minecraft/client/DeltaTracker;ZLnet/minecraft/client/renderer/state/level/CameraRenderState;Lorg/joml/Matrix4fc;Lcom/mojang/blaze3d/buffers/GpuBufferSlice;Lorg/joml/Vector4f;ZLnet/minecraft/client/renderer/chunk/ChunkSectionsToRender;)V"))
	private void iris$renderLevel(LevelRenderer instance, GraphicsResourceAllocator resourceAllocator, DeltaTracker deltaTracker, boolean renderOutline, CameraRenderState cameraState, Matrix4fc modelViewMatrix, GpuBufferSlice terrainFog, Vector4f fogColor, boolean shouldRenderSky, ChunkSectionsToRender chunkSectionsToRender, Operation<Void> original, @Local(name = "player") LocalPlayer player) {
		if (areShadersOn) {
			((Matrix4f) modelViewMatrix).mulLocal(bobStack); // need `bob * modelView` not `modelView * bob`
		}

		original.call(instance, resourceAllocator, deltaTracker, renderOutline, cameraState, modelViewMatrix, terrainFog, fogColor, shouldRenderSky, chunkSectionsToRender);
	}
}
