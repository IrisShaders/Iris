package net.coderbot.iris.mixin.normals;

import com.mojang.blaze3d.systems.RenderSystem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Matrix4f;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
@Mixin(WorldRenderer.class)
public class MixinWorldRenderer {
	private static final String RENDER = "render(Lnet/minecraft/client/util/math/MatrixStack;FJZLnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/GameRenderer;Lnet/minecraft/client/render/LightmapTextureManager;Lnet/minecraft/util/math/Matrix4f;)V";
	private static final String CHECK_EMPTY = "net/minecraft/client/render/WorldRenderer.checkEmpty(Lnet/minecraft/client/util/math/MatrixStack;)V";
	private static final String PROFILER_SWAP = "net/minecraft/util/profiler/Profiler.swap(Ljava/lang/String;)V";

	@Inject(method = RENDER, at = {
			@At(value = "INVOKE_STRING", target = PROFILER_SWAP, args = "ldc=entities"),
			@At(value = "INVOKE_STRING", target = PROFILER_SWAP, args = "ldc=blockentities")
	})
	private void iris$resetNormalMatrix(MatrixStack matrices, float tickDelta, long limitTime,
										   boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer,
										   LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f, CallbackInfo ci) {
		// Don't bake the camera rotation / position into the vertices and vertex normals
		matrices.push();

		RenderSystem.pushMatrix();
		RenderSystem.loadIdentity();
		RenderSystem.multMatrix(matrices.peek().getModel());

		matrices.peek().getModel().loadIdentity();
		matrices.peek().getNormal().loadIdentity();
	}

	@Inject(method = RENDER, at = @At(value = "INVOKE", target = CHECK_EMPTY), slice = @Slice(
			from = @At(value = "INVOKE_STRING", target = PROFILER_SWAP, args = "ldc=entities"),
			  to = @At(value = "INVOKE_STRING", target = PROFILER_SWAP, args = "ldc=destroyProgress")
	))
	private void iris$preCheckEmpty(MatrixStack matrices, float tickDelta, long limitTime,
										   boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer,
										   LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f, CallbackInfo ci) {
		// Pop our modified normal matrix from the stack
		matrices.pop();

		// Pop the matrix from the GL stack
		RenderSystem.popMatrix();
	}
}
