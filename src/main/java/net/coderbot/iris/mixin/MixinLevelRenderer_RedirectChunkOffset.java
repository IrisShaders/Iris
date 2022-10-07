package net.coderbot.iris.mixin;

import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import net.coderbot.iris.pipeline.newshader.ExtendedShader;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Group;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = LevelRenderer.class, priority = 1010)
public class MixinLevelRenderer_RedirectChunkOffset {
	@Group(name = "iris_MixinRedirectChunkOffset", min = 1, max = 2)
	@Inject(method = "renderChunkLayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/RenderType;setupRenderState()V", shift = At.Shift.AFTER), require = 0)
	private void setProjModelView(RenderType arg, PoseStack arg2, double d, double e, double f, Matrix4f arg3, CallbackInfo ci) {
		if (RenderSystem.getShader() instanceof ExtendedShader extendedShader) {
			extendedShader.setProjectionModelViewOverride(arg2, arg3);
		}
	}

	@Group(name = "iris_MixinRedirectChunkOffset", min = 1, max = 2)
	@Redirect(method = "renderChunkLayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/chunk/ChunkRenderDispatcher$RenderChunk;getOrigin()Lnet/minecraft/core/BlockPos;"), require = 0)
	private BlockPos iris$setChunkOffset(ChunkRenderDispatcher.RenderChunk instance) {
		BlockPos origin = instance.getOrigin();
		if (RenderSystem.getShader() instanceof ExtendedShader extendedShader) {
			Vec3 camera = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
			extendedShader.setChunkOffset((float) (origin.getX() - camera.x), (float) (origin.getY() - camera.y), (float) (origin.getZ() - camera.z));
		}
		return origin;
	}

	@Group(name = "iris_MixinRedirectChunkOffset", min = 1, max = 2)
	@Inject(method = "renderChunkLayer",
		at = @At(value = "INVOKE",
			target = "me/jellysquid/mods/sodium/client/gl/device/RenderDevice.enterManagedCode ()V",
			remap = false),
		require = 0)
	private void iris$cannotInject(RenderType arg, PoseStack arg2, double d, double e, double f, Matrix4f arg3, CallbackInfo ci) {
		// Dummy injection just to assert that either Sodium is present, or the vanilla injection passed.
	}
}
