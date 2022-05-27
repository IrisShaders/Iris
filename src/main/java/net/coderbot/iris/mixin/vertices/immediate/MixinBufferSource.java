package net.coderbot.iris.mixin.vertices.immediate;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.coderbot.iris.vertices.ExtendingBufferBuilder;
import net.coderbot.iris.vertices.ImmediateState;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Quick optimization to disable the extended vertex format outside of level rendering if we're using a BufferSource.
 * This is a heuristic that should hopefully work almost always because of how people use BufferSource.
 */
@Mixin(MultiBufferSource.BufferSource.class)
public class MixinBufferSource {
	@Redirect(method = "getBuffer",
		at = @At(value = "INVOKE",
			target = "com/mojang/blaze3d/vertex/BufferBuilder.begin (Lcom/mojang/blaze3d/vertex/VertexFormat$Mode;Lcom/mojang/blaze3d/vertex/VertexFormat;)V"))
	private void iris$redirectBegin(BufferBuilder bufferBuilder, VertexFormat.Mode drawMode, VertexFormat vertexFormat) {
		if (iris$notRenderingLevel()) {
			((ExtendingBufferBuilder) bufferBuilder).iris$beginWithoutExtending(drawMode, vertexFormat);
		} else {
			bufferBuilder.begin(drawMode, vertexFormat);
		}
	}

	@Inject(method = "endBatch(Lnet/minecraft/client/renderer/RenderType;)V",
		at = @At(value = "INVOKE",
			target = "net/minecraft/client/renderer/RenderType.end (Lcom/mojang/blaze3d/vertex/BufferBuilder;III)V"))
	private void iris$beforeFlushBuffer(RenderType renderType, CallbackInfo ci) {
		if (iris$notRenderingLevel()) {
			ImmediateState.renderWithExtendedVertexFormat = false;
		}
	}

	@Inject(method = "endBatch(Lnet/minecraft/client/renderer/RenderType;)V",
		at = @At(value = "INVOKE",
			target = "net/minecraft/client/renderer/RenderType.end (Lcom/mojang/blaze3d/vertex/BufferBuilder;III)V",
			shift = At.Shift.AFTER))
	private void iris$afterFlushBuffer(RenderType renderType, CallbackInfo ci) {
		if (iris$notRenderingLevel()) {
			ImmediateState.renderWithExtendedVertexFormat = true;
		}
	}

	private boolean iris$notRenderingLevel() {
		return !ImmediateState.isRenderingLevel;
	}
}
