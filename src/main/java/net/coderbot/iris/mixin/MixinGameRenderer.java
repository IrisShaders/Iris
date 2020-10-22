package net.coderbot.iris.mixin;

import net.coderbot.iris.uniforms.CapturedRenderingState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.render.GameRenderer;
import net.minecraft.util.math.Matrix4f;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Mixin(GameRenderer.class)
@Environment(EnvType.CLIENT)
public class MixinGameRenderer {
	// TODO: This probably won't be compatible with mods that directly mess with the GL projection matrix.
	// https://github.com/jellysquid3/sodium-fabric/blob/1df506fd39dac56bb410725c245e6e51208ec732/src/main/java/me/jellysquid/mods/sodium/client/render/chunk/shader/ChunkProgram.java#L56
	@Inject(method = "loadProjectionMatrix(Lnet/minecraft/util/math/Matrix4f;)V", at = @At("HEAD"))
	private void iris$captureProjectionMatrix(Matrix4f projectionMatrix, CallbackInfo callback) {
		CapturedRenderingState.INSTANCE.setGbufferProjection(projectionMatrix);
	}
}
