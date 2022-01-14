package net.coderbot.iris.compat.sodium.mixin.shader_overrides;

import me.jellysquid.mods.sodium.client.gl.buffer.GlMutableBuffer;
import me.jellysquid.mods.sodium.client.gl.shader.GlProgram;
import me.jellysquid.mods.sodium.client.render.chunk.ChunkRenderMatrices;
import me.jellysquid.mods.sodium.client.render.chunk.RegionChunkRenderer;
import me.jellysquid.mods.sodium.client.render.chunk.shader.ChunkShaderInterface;
import net.coderbot.iris.compat.sodium.impl.shader_overrides.ShaderChunkRendererExt;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(RegionChunkRenderer.class)
public abstract class MixinRegionChunkRenderer implements ShaderChunkRendererExt {
	@Redirect(method = "render", remap = false,
			at = @At(value = "INVOKE",
					target = "me/jellysquid/mods/sodium/client/gl/shader/GlProgram.getInterface ()Ljava/lang/Object;"))
	private Object iris$getInterface(GlProgram<?> program) {
		if (program == null) {
			// Iris sentinel null
			return null;
		} else {
			return program.getInterface();
		}
	}

	@Redirect(method = "render",
			at = @At(value = "INVOKE",
					target = "Lme/jellysquid/mods/sodium/client/render/chunk/shader/ChunkShaderInterface;setProjectionMatrix(Lorg/joml/Matrix4f;)V"), remap = false)
	private void iris$setProjectionMatrix(ChunkShaderInterface itf, Matrix4f matrix) {
		if (itf != null) {
			itf.setProjectionMatrix(matrix);
		} else {
			iris$getOverride().getInterface().setProjectionMatrix(matrix);
		}
	}

	@Redirect(method = "render",
			at = @At(value = "INVOKE",
					target = "Lme/jellysquid/mods/sodium/client/render/chunk/shader/ChunkShaderInterface;setModelViewMatrix(Lorg/joml/Matrix4f;)V"), remap = false)
	private void iris$setModelViewMatrix(ChunkShaderInterface itf, Matrix4f matrix) {
		if (itf != null) {
			itf.setModelViewMatrix(matrix);
		} else {
			iris$getOverride().getInterface().setModelViewMatrix(matrix);
		}
	}

	@Redirect(method = "render", remap = false,
			at = @At(value = "INVOKE",
					target = "me/jellysquid/mods/sodium/client/render/chunk/shader/ChunkShaderInterface.setDrawUniforms (Lme/jellysquid/mods/sodium/client/gl/buffer/GlMutableBuffer;)V"))
	private void iris$setDrawUniforms(ChunkShaderInterface itf, GlMutableBuffer buffer) {
		if (itf != null) {
			itf.setDrawUniforms(buffer);
		} else {
			iris$getOverride().getInterface().setDrawUniforms(buffer);
		}
	}

	@Redirect(method = "setModelMatrixUniforms",
			at = @At(value = "INVOKE",
					target = "Lme/jellysquid/mods/sodium/client/render/chunk/shader/ChunkShaderInterface;setRegionOffset(FFF)V"), remap = false)
	private void iris$setRegionOffset(ChunkShaderInterface itf, float x, float y, float z) {
		if (itf != null) {
			itf.setRegionOffset(x, y, z);
		} else {
			iris$getOverride().getInterface().setRegionOffset(x, y, z);
		}
	}
}
