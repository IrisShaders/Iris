package net.irisshaders.iris.mixin;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.debug.ChunkBorderRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Slice;

@Mixin(ChunkBorderRenderer.class)
public class MixinChunkBorderRenderer {
	@WrapWithCondition(
		method = "render",
		at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/VertexConsumer;endVertex()V"),
		slice = @Slice(
			from = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack$Pose;pose()Lorg/joml/Matrix4f;"),
			to = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/debug/ChunkBorderRenderer;CELL_BORDER:I", ordinal = 0)
		)
	)
	private boolean isCameraChunk(VertexConsumer instance, @Local(ordinal = 0) int k, @Local(ordinal = 1) int l) {
		return !((k == 0 || k == 16) && (l == 0 || l == 16));
	}

	@WrapWithCondition(
		method = "render",
		at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/VertexConsumer;endVertex()V"),
		slice = @Slice(
			from = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;getMinBuildHeight()I", ordinal = 1),
			to = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/MultiBufferSource;getBuffer(Lnet/minecraft/client/renderer/RenderType;)Lcom/mojang/blaze3d/vertex/VertexConsumer;", ordinal = 1)
		)
	)
	private boolean isSubChunkBorder(VertexConsumer instance, @Local(ordinal = 0) int k) {
		return k % 16 != 0;
	}
}
