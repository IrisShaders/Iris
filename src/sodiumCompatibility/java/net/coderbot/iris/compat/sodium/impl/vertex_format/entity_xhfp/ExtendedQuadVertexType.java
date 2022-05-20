package net.coderbot.iris.compat.sodium.impl.vertex_format.entity_xhfp;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import me.jellysquid.mods.sodium.client.model.vertex.buffer.VertexBufferView;
import me.jellysquid.mods.sodium.client.model.vertex.formats.quad.QuadVertexSink;
import me.jellysquid.mods.sodium.client.model.vertex.formats.quad.writer.QuadVertexWriterFallback;
import me.jellysquid.mods.sodium.client.model.vertex.type.BlittableVertexType;
import me.jellysquid.mods.sodium.client.model.vertex.type.VanillaVertexType;
import net.coderbot.iris.vertices.IrisVertexFormats;

public class ExtendedQuadVertexType implements VanillaVertexType<QuadVertexSink>, BlittableVertexType<QuadVertexSink> {
	public static final ExtendedQuadVertexType INSTANCE = new ExtendedQuadVertexType();

	@Override
	public QuadVertexSink createFallbackWriter(VertexConsumer vertexConsumer) {
		return new QuadVertexWriterFallback(vertexConsumer);
	}

	@Override
	public QuadVertexSink createBufferWriter(VertexBufferView buffer, boolean direct) {
		return direct ? new EntityVertexBufferWriterUnsafe(buffer) : new EntityVertexBufferWriterNio(buffer);
	}

	@Override
	public VertexFormat getVertexFormat() {
		return IrisVertexFormats.ENTITY;
	}

	public BlittableVertexType<QuadVertexSink> asBlittable() {
		return this;
	}
}
