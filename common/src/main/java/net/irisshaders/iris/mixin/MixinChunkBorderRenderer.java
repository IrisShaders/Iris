package net.irisshaders.iris.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.debug.ChunkBorderRenderer;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;

@Mixin(ChunkBorderRenderer.class)
public class MixinChunkBorderRenderer {
	private VertexConsumer fakeConsumer = new VertexConsumer() {
		@Override
		public VertexConsumer addVertex(float x, float y, float z) {
			return this;
		}

		@Override
		public VertexConsumer setColor(int red, int green, int blue, int alpha) {
			return this;
		}

		@Override
		public VertexConsumer setUv(float u, float v) {
			return this;
		}

		@Override
		public VertexConsumer setUv1(int u, int v) {
			return this;
		}

		@Override
		public VertexConsumer setUv2(int u, int v) {
			return this;
		}

		@Override
		public VertexConsumer setNormal(float normalX, float normalY, float normalZ) {
			return this;
		}
	};

	@Redirect(
		method = "render",
		at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/VertexConsumer;addVertex(Lorg/joml/Matrix4f;FFF)Lcom/mojang/blaze3d/vertex/VertexConsumer;"),
		slice = @Slice(
			from = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack$Pose;pose()Lorg/joml/Matrix4f;"),
			to = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/debug/ChunkBorderRenderer;CELL_BORDER:I", ordinal = 0)
		)
	)
	private VertexConsumer isCameraChunk(VertexConsumer instance, Matrix4f pose, float x, float y, float z, @Local(ordinal = 0) int k, @Local(ordinal = 1) int l) {
		if (!((k == 0 || k == 16) && (l == 0 || l == 16))) {
			return instance.addVertex(pose, x,y ,z);
		} else {
			return fakeConsumer;
		}
	}

	@Redirect(
		method = "render",
		at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/VertexConsumer;addVertex(Lorg/joml/Matrix4f;FFF)Lcom/mojang/blaze3d/vertex/VertexConsumer;"),
		slice = @Slice(
			from = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;getMinBuildHeight()I", ordinal = 1),
			to = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/MultiBufferSource;getBuffer(Lnet/minecraft/client/renderer/RenderType;)Lcom/mojang/blaze3d/vertex/VertexConsumer;", ordinal = 1)
		)
	)
	private VertexConsumer isSubChunkBorder(VertexConsumer instance, Matrix4f pose, float x, float y, float z, @Local(ordinal = 0) int k) {
		if (k % 16 != 0) {
			return instance.addVertex(pose, x,y ,z);
		} else {
			return fakeConsumer;
		}
	}
}
