package net.irisshaders.iris.compat.sodium.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.caffeinemc.mods.sodium.api.util.NormI8;
import net.caffeinemc.mods.sodium.api.vertex.buffer.VertexBufferWriter;
import net.caffeinemc.mods.sodium.api.vertex.format.common.ColorVertex;
import net.caffeinemc.mods.sodium.client.render.immediate.CloudRenderer;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.api.v0.IrisApi;
import net.irisshaders.iris.pipeline.ShaderRenderingPipeline;
import net.irisshaders.iris.pipeline.WorldRenderingPipeline;
import net.irisshaders.iris.pipeline.programs.ShaderKey;
import net.irisshaders.iris.vertices.IrisVertexFormats;
import net.irisshaders.iris.vertices.sodium.CloudVertex;
import net.minecraft.client.renderer.ShaderInstance;
import org.jetbrains.annotations.Nullable;
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
	@Unique
	private static final int[] NORMALS = new int[]{
		NormI8.pack(0.0f, -1.0f, 0.0f), // NEG_Y
		NormI8.pack(0.0f, 1.0f, 0.0f), // POS_Y
		NormI8.pack(-1.0f, 0.0f, 0.0f), //	NEG_X
		NormI8.pack(1.0f, 0.0f, 0.0f), // POS_X
		NormI8.pack(0.0f, 0.0f, -1.0f), // NEG_Z
		NormI8.pack(0.0f, 0.0f, 1.0f) // POS_Z
	};

	@Unique
	private static int computedNormal;

	@Shadow
	private ShaderInstance shaderProgram;

	@Shadow(remap = false)
	@Nullable
	private CloudRenderer.@Nullable CloudGeometry cachedGeometry;

	@Unique
	private @Nullable CloudRenderer.CloudGeometry cachedGeometryIris;

	@Inject(method = "writeVertex", at = @At("HEAD"), cancellable = true, remap = false)
	private static void writeIrisVertex(long buffer, float x, float y, float z, int color, CallbackInfoReturnable<Long> cir) {
		if (Iris.isPackInUseQuick()) {
			CloudVertex.put(buffer, x, y, z, color, computedNormal);
			cir.setReturnValue(buffer + 20L);
		}
	}

	@Inject(method = "emitCellGeometry2D", at = @At("HEAD"), remap = false)
	private static void computeNormal2D(VertexBufferWriter writer, int faces, int color, float x, float z, CallbackInfo ci) {
		computedNormal = NORMALS[0];
	}

	@Inject(method = "emitCellGeometry3D", at = @At(value = "INVOKE", target = "Lnet/caffeinemc/mods/sodium/client/render/immediate/CloudRenderer$CloudFace;ordinal()I"), remap = false)
	private static void computeNormal3D(VertexBufferWriter writer, int visibleFaces, int baseColor, float posX, float posZ, boolean interior, CallbackInfo ci, @Local(ordinal = 4) int face) {
		computedNormal = NORMALS[face];
	}

	@ModifyArg(remap = false, method = "emitCellGeometry3D", at = @At(value = "INVOKE", target = "Lorg/lwjgl/system/MemoryStack;nmalloc(I)J"))
	private static int allocateNewSize(int size) {
		return Iris.isPackInUseQuick() ? 480 : size;
	}

	@ModifyArg(method = "rebuildGeometry", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/Tesselator;begin(Lcom/mojang/blaze3d/vertex/VertexFormat$Mode;Lcom/mojang/blaze3d/vertex/VertexFormat;)Lcom/mojang/blaze3d/vertex/BufferBuilder;"), index = 1)
	private static VertexFormat rebuild(VertexFormat p_350837_) {
		return Iris.isPackInUseQuick() ? IrisVertexFormats.CLOUDS : p_350837_;
	}

	@ModifyArg(remap = false, method = "emitCellGeometry3D", at = @At(value = "INVOKE", target = "Lnet/caffeinemc/mods/sodium/api/vertex/buffer/VertexBufferWriter;push(Lorg/lwjgl/system/MemoryStack;JILcom/mojang/blaze3d/vertex/VertexFormat;)V"), index = 3)
	private static VertexFormat modifyArgIris(VertexFormat vertexFormatDescription) {
		if (Iris.isPackInUseQuick()) {
			return IrisVertexFormats.CLOUDS;
		} else {
			return ColorVertex.FORMAT;
		}
	}

	@ModifyArg(remap = false, method = "emitCellGeometry2D", at = @At(value = "INVOKE", target = "Lorg/lwjgl/system/MemoryStack;nmalloc(I)J"))
	private static int allocateNewSize2D(int size) {
		return Iris.isPackInUseQuick() ? 80 : size;
	}

	@ModifyArg(remap = false, method = "emitCellGeometry2D", at = @At(value = "INVOKE", target = "Lnet/caffeinemc/mods/sodium/api/vertex/buffer/VertexBufferWriter;push(Lorg/lwjgl/system/MemoryStack;JILcom/mojang/blaze3d/vertex/VertexFormat;)V"), index = 3)
	private static VertexFormat modifyArgIris2D(VertexFormat vertexFormatDescription) {
		if (Iris.isPackInUseQuick()) {
			return IrisVertexFormats.CLOUDS;
		} else {
			return ColorVertex.FORMAT;
		}
	}

	@Redirect(method = "render", at = @At(remap = false, value = "FIELD", target = "Lnet/caffeinemc/mods/sodium/client/render/immediate/CloudRenderer;cachedGeometry:Lnet/caffeinemc/mods/sodium/client/render/immediate/CloudRenderer$CloudGeometry;", ordinal = 0))
	private CloudRenderer.@Nullable CloudGeometry changeGeometry(CloudRenderer instance) {
		if (Iris.isPackInUseQuick()) {
			return cachedGeometryIris;
		} else {
			return cachedGeometry;
		}
	}

	@Redirect(method = "render", at = @At(remap = false, value = "FIELD", target = "Lnet/caffeinemc/mods/sodium/client/render/immediate/CloudRenderer;cachedGeometry:Lnet/caffeinemc/mods/sodium/client/render/immediate/CloudRenderer$CloudGeometry;", ordinal = 1))
	private void changeGeometry2(CloudRenderer instance, CloudRenderer.CloudGeometry value) {
		if (Iris.isPackInUseQuick()) {
			cachedGeometryIris = value;
		} else {
			cachedGeometry = value;
		}
	}

	@Redirect(method = "render", at = @At(value = "FIELD", target = "Lnet/caffeinemc/mods/sodium/client/render/immediate/CloudRenderer;shaderProgram:Lnet/minecraft/client/renderer/ShaderInstance;"))
	private ShaderInstance changeShader(CloudRenderer instance) {
		return getClouds();
	}

	@Unique
	private ShaderInstance getClouds() {
		WorldRenderingPipeline pipeline = Iris.getPipelineManager().getPipelineNullable();

		if (pipeline instanceof ShaderRenderingPipeline) {
			return ((ShaderRenderingPipeline) pipeline).getShaderMap().getShader(ShaderKey.CLOUDS_SODIUM);
		}

		return shaderProgram;
	}
}
