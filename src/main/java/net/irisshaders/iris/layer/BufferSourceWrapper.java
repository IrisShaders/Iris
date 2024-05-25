package net.irisshaders.iris.layer;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.irisshaders.batchedentityrendering.impl.Groupable;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;

import java.util.function.Function;

public class BufferSourceWrapper implements MultiBufferSource, Groupable {
	private final MultiBufferSource bufferSource;
	private final Function<RenderType, RenderType> typeChanger;

	public BufferSourceWrapper(MultiBufferSource bufferSource, Function<RenderType, RenderType> typeChanger) {
		this.bufferSource = bufferSource;
		this.typeChanger = typeChanger;
	}

	@Override
	public void startGroup() {
		if (bufferSource instanceof Groupable groupable) {
			groupable.startGroup();
		}
	}

	@Override
	public boolean maybeStartGroup() {
		if (bufferSource instanceof Groupable groupable) {
			return groupable.maybeStartGroup();
		}
		return false;
	}

	@Override
	public void endGroup() {
		if (bufferSource instanceof Groupable groupable) {
			groupable.endGroup();
		}
	}

	@Override
	public VertexConsumer getBuffer(RenderType renderType) {
		return bufferSource.getBuffer(typeChanger.apply(renderType));
	}
}
