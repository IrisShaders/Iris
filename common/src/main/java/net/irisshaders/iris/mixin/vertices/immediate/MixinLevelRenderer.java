package net.irisshaders.iris.mixin.vertices.immediate;

import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import net.irisshaders.iris.vertices.ImmediateState;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Uses a priority of 999 to apply before the main Iris mixins to draw entities before deferred runs.
@Mixin(value = LevelRenderer.class, priority = 999)
public class MixinLevelRenderer {
	@Inject(method = "render", at = @At("HEAD"))
	private void iris$immediateStateBeginLevelRender(GraphicsResourceAllocator resourceAllocator, boolean renderOutline,
		CameraRenderState cameraState, GpuBufferSlice terrainFog, Vector4f fogColor, boolean shouldRenderSky,
		boolean consistentDepthRequired, CallbackInfo ci) {
		ImmediateState.isRenderingLevel = true;
	}

	@Inject(method = "render", at = @At("RETURN"))
	private void iris$immediateStateEndLevelRender(GraphicsResourceAllocator resourceAllocator, boolean renderOutline,
		CameraRenderState cameraState, GpuBufferSlice terrainFog, Vector4f fogColor, boolean shouldRenderSky,
		boolean consistentDepthRequired, CallbackInfo ci) {
		ImmediateState.isRenderingLevel = false;
	}
}
