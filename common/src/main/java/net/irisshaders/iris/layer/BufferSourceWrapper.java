package net.irisshaders.iris.layer;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;

import java.util.function.Function;

public class BufferSourceWrapper implements MultiBufferSource {
	private final MultiBufferSource bufferSource;
	private final Function<RenderType, RenderType> typeChanger;

	public BufferSourceWrapper(MultiBufferSource bufferSource, Function<RenderType, RenderType> typeChanger) {
		this.bufferSource = bufferSource;
		this.typeChanger = typeChanger;
	}

	public MultiBufferSource getOriginal() {
		return bufferSource;
	}

	@Override
	public VertexConsumer getBuffer(RenderType renderType) {
		return bufferSource.getBuffer(typeChanger.apply(renderType));
	}
}
