package net.irisshaders.iris.mixin.vertices.immediate;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.irisshaders.iris.vertices.ImmediateState;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Quick optimization to disable the extended vertex format outside of level rendering if we're using a BufferSource.
 * This is a heuristic that should hopefully work almost always because of how people use BufferSource.
 */
@Mixin(MultiBufferSource.BufferSource.class)
public class MixinBufferSource {
	@WrapOperation(method = "getBuffer",
		at = @At(value = "NEW",
			target = "(Lcom/mojang/blaze3d/vertex/ByteBufferBuilder;Lcom/mojang/blaze3d/vertex/VertexFormat$Mode;Lcom/mojang/blaze3d/vertex/VertexFormat;)Lcom/mojang/blaze3d/vertex/BufferBuilder;"))
	private BufferBuilder iris$redirectBegin(ByteBufferBuilder byteBufferBuilder, VertexFormat.Mode mode, VertexFormat vertexFormat, Operation<BufferBuilder> original) {
		ImmediateState.skipExtension.set(iris$notRenderingLevel());
		BufferBuilder builder = original.call(byteBufferBuilder, mode, vertexFormat);
		ImmediateState.skipExtension.set(false);

		return builder;
	}

	@Inject(method = "endBatch(Lnet/minecraft/client/renderer/RenderType;Lcom/mojang/blaze3d/vertex/BufferBuilder;)V",
		at = @At(value = "INVOKE",
			target = "Lnet/minecraft/client/renderer/RenderType;draw(Lcom/mojang/blaze3d/vertex/MeshData;)V"))
	private void iris$beforeFlushBuffer(RenderType renderType, BufferBuilder bufferBuilder, CallbackInfo ci) {
		if (iris$notRenderingLevel()) {
			ImmediateState.renderWithExtendedVertexFormat = false;
		}
	}

	@Inject(method = "endBatch(Lnet/minecraft/client/renderer/RenderType;Lcom/mojang/blaze3d/vertex/BufferBuilder;)V",
		at = @At(value = "INVOKE",
			target = "Lnet/minecraft/client/renderer/RenderType;draw(Lcom/mojang/blaze3d/vertex/MeshData;)V",
			shift = At.Shift.AFTER))
	private void iris$afterFlushBuffer(RenderType renderType, BufferBuilder bufferBuilder, CallbackInfo ci) {
		if (iris$notRenderingLevel()) {
			ImmediateState.renderWithExtendedVertexFormat = true;
		}
	}

	@Unique
	private boolean iris$notRenderingLevel() {
		return !ImmediateState.isRenderingLevel;
	}
}
