package net.coderbot.iris.mixin.gbuffer;

import com.mojang.blaze3d.systems.RenderSystem;
import net.coderbot.iris.Iris;
import org.lwjgl.opengl.GL11C;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Matrix4f;

@Mixin(WorldRenderer.class)
public class MixinWorldRenderer {
	private static final String RENDER = "render(Lnet/minecraft/client/util/math/MatrixStack;FJZLnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/GameRenderer;Lnet/minecraft/client/render/LightmapTextureManager;Lnet/minecraft/util/math/Matrix4f;)V";

	@Inject(method = RENDER, at = @At("HEAD"))
	private void iris$beginWorldRender(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f, CallbackInfo callback) {
		Framebuffer main = MinecraftClient.getInstance().getFramebuffer();
		Iris.getCompositeRenderer().renderTargets.resizeIfNeeded(main.textureWidth, main.textureHeight);

		// The framebuffer will be cleared by WorldRenderer
		// TODO: I assume that most buffers shouldn't be cleared to the fog color...
		Iris.getCompositeRenderer().clearEverythingBuffer.bind();
	}

	@Inject(method = RENDER, at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;clear(IZ)V"))
	private void iris$afterClear(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f, CallbackInfo callback) {
		Iris.getCompositeRenderer().clearAltBuffers.bind();
		// Not clearing the depth buffer since there's only one of those and it was already cleared
		RenderSystem.clearColor(0.0f, 0.0f, 0.0f, 0.0f);
		RenderSystem.clear(GL11C.GL_COLOR_BUFFER_BIT, MinecraftClient.IS_SYSTEM_MAC);

		Iris.getCompositeRenderer().clearEverythingBuffer.bind();
	}
}
