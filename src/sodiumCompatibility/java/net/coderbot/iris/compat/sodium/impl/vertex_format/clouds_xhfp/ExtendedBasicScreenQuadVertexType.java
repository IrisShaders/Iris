package net.coderbot.iris.compat.sodium.impl.vertex_format.clouds_xhfp;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import me.jellysquid.mods.sodium.client.model.vertex.buffer.VertexBufferView;
import me.jellysquid.mods.sodium.client.model.vertex.formats.screen_quad.BasicScreenQuadVertexSink;
import me.jellysquid.mods.sodium.client.model.vertex.formats.screen_quad.writer.BasicScreenQuadVertexWriterFallback;
import me.jellysquid.mods.sodium.client.model.vertex.type.BlittableVertexType;
import me.jellysquid.mods.sodium.client.model.vertex.type.VanillaVertexType;
import net.coderbot.iris.vertices.IrisVertexFormats;

public class ExtendedBasicScreenQuadVertexType implements VanillaVertexType<BasicScreenQuadVertexSink>, BlittableVertexType<BasicScreenQuadVertexSink> {
	public static final ExtendedBasicScreenQuadVertexType INSTANCE = new ExtendedBasicScreenQuadVertexType();

	@Override
	public BasicScreenQuadVertexSink createFallbackWriter(VertexConsumer vertexConsumer) {
		return new BasicScreenQuadVertexWriterFallback(vertexConsumer);
	}

	@Override
	public BasicScreenQuadVertexSink createBufferWriter(VertexBufferView buffer, boolean direct) {
		return direct ? new CloudsVertexBufferWriterUnsafe(buffer) : new CloudsVertexBufferWriterNio(buffer);
	}

	@Override
	public VertexFormat getVertexFormat() {
		return IrisVertexFormats.CLOUDS;
	}

	public BlittableVertexType<BasicScreenQuadVertexSink> asBlittable() {
		return this;
	}
}
