package net.irisshaders.iris.compat.sodium.mixin.clouds;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexBuffer;
import net.caffeinemc.mods.sodium.api.vertex.format.VertexFormatDescription;
import net.caffeinemc.mods.sodium.api.vertex.format.common.ColorVertex;
import net.caffeinemc.mods.sodium.client.render.immediate.CloudRenderer;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.api.v0.IrisApi;
import net.irisshaders.iris.compat.sodium.impl.vertex_format.entity_xhfp.CloudVertex;
import net.irisshaders.iris.pipeline.ShaderRenderingPipeline;
import net.irisshaders.iris.pipeline.WorldRenderingPipeline;
import net.irisshaders.iris.pipeline.programs.ShaderKey;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.ShaderInstance;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CloudRenderer.class)
public abstract class MixinCloudRenderer {
	@Shadow
	private ShaderInstance shaderProgram;
	@Shadow
	@Nullable
	private CloudRenderer.@Nullable CloudGeometry cachedGeometry;
	@Unique
	private VertexBuffer vertexBufferWithNormals;
	@Unique
	private int prevCenterCellXIris, prevCenterCellYIris, cachedRenderDistanceIris;
	@Unique
	private @Nullable CloudRenderer.CloudGeometry cachedGeometryIris;

	@Inject(method = "writeVertex", at = @At("HEAD"), cancellable = true, remap = false)
	private static void writeIrisVertex(long buffer, float x, float y, float z, int color, CallbackInfoReturnable<Long> cir) {
		if (IrisApi.getInstance().isShaderPackInUse()) {
			CloudVertex.write(buffer, x, y, z, color);
			cir.setReturnValue(buffer + 20L);
		}
	}

	@Inject(method = "render", at = @At(value = "HEAD"), cancellable = true)
	private void buildIrisVertexBuffer(Camera camera, ClientLevel level, Matrix4f projectionMatrix, PoseStack poseStack, float ticks, float tickDelta, CallbackInfo ci) {
		if (IrisApi.getInstance().isShaderPackInUse()) {
			//ci.cancel();
			//renderIris(level, player, matrices, projectionMatrix, ticks, tickDelta, cameraX, cameraY, cameraZ);
		}
	}

	@Redirect(method = "render", at = @At(value = "FIELD", target = "Lnet/caffeinemc/mods/sodium/client/render/immediate/CloudRenderer;cachedGeometry:Lnet/caffeinemc/mods/sodium/client/render/immediate/CloudRenderer$CloudGeometry;", ordinal = 0))
	private CloudRenderer.@Nullable CloudGeometry changeGeometry(CloudRenderer instance) {
		if (IrisApi.getInstance().isShaderPackInUse()) {
			return cachedGeometryIris;
		} else {
			return cachedGeometry;
		}
	}

	@Redirect(method = "render", at = @At(value = "FIELD", target = "Lnet/caffeinemc/mods/sodium/client/render/immediate/CloudRenderer;cachedGeometry:Lnet/caffeinemc/mods/sodium/client/render/immediate/CloudRenderer$CloudGeometry;", ordinal = 1))
	private void changeGeometry2(CloudRenderer instance, CloudRenderer.CloudGeometry value) {
		if (IrisApi.getInstance().isShaderPackInUse()) {
			cachedGeometryIris = value;
		} else {
			cachedGeometry = value;
		}
	}

	@Redirect(method = "render", at = @At(value = "FIELD", target = "Lnet/caffeinemc/mods/sodium/client/render/immediate/CloudRenderer;shaderProgram:Lnet/minecraft/client/renderer/ShaderInstance;"))
	private ShaderInstance changeShader(CloudRenderer instance) {
		return getClouds();
	}

	@ModifyArg(remap = false, method = "emitCellGeometry3D", at = @At(value = "INVOKE", target = "Lorg/lwjgl/system/MemoryStack;nmalloc(I)J"))
	private static int allocateNewSize(int size) {
		return IrisApi.getInstance().isShaderPackInUse() ? 480 : size;
	}

	@ModifyArg(remap = false, method = "emitCellGeometry3D", at = @At(value = "INVOKE", target = "Lnet/caffeinemc/mods/sodium/api/vertex/buffer/VertexBufferWriter;push(Lorg/lwjgl/system/MemoryStack;JILnet/caffeinemc/mods/sodium/api/vertex/format/VertexFormatDescription;)V"), index = 3)
	private static VertexFormatDescription modifyArgIris(VertexFormatDescription vertexFormatDescription) {
		if (IrisApi.getInstance().isShaderPackInUse()) {
			return CloudVertex.FORMAT;
		} else {
			return ColorVertex.FORMAT;
		}
	}

	private ShaderInstance getClouds() {
		WorldRenderingPipeline pipeline = Iris.getPipelineManager().getPipelineNullable();

		if (pipeline instanceof ShaderRenderingPipeline) {
			return ((ShaderRenderingPipeline) pipeline).getShaderMap().getShader(ShaderKey.CLOUDS_SODIUM);
		}

		return shaderProgram;
	}
}
