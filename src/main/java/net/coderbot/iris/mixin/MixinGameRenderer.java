package net.coderbot.iris.mixin;

import com.mojang.blaze3d.platform.GlUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;

import net.coderbot.iris.Iris;
import net.coderbot.iris.pipeline.HandRenderer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.server.packs.resources.ResourceManager;

@Mixin(GameRenderer.class)
@Environment(EnvType.CLIENT)
public class MixinGameRenderer {
	@Inject(method = "<init>", at = @At("TAIL"))
	private void iris$logSystem(Minecraft client, ResourceManager resourceManager, RenderBuffers bufferBuilderStorage,
								CallbackInfo ci) {
		Iris.logger.info("Hardware information:");
		Iris.logger.info("CPU: " + GlUtil.getCpuInfo());
		Iris.logger.info("GPU: " + GlUtil.getRenderer() + " (Supports OpenGL " + GlUtil.getOpenGLVersion() + ")");
		Iris.logger.info("OS: " + System.getProperty("os.name"));
	}

	@Inject(method = "getProjectionMatrix", at = @At(value = "INVOKE", target = "Lcom/mojang/math/Matrix4f;multiply(Lcom/mojang/math/Matrix4f;)V"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void scaleHandDepth(Camera camera, float f, boolean bl, CallbackInfoReturnable<Matrix4f> cir, PoseStack poseStack) {
        if (HandRenderer.isActive()) {
            // This value is taken directly from Shaders Mod.
            poseStack.scale(1F, 1F, 0.125F);
        }
    }

	@Redirect(method = "renderLevel", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/GameRenderer;renderHand:Z"))
	private boolean disableVanillaHandRendering(GameRenderer gameRenderer) {
		return false;
	}
}
