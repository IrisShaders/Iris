package net.coderbot.iris.compat.sodium.impl.vertex_format.entity_xhfp;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.caffeinemc.sodium.interop.vanilla.vertex.VanillaVertexType;
import net.caffeinemc.sodium.interop.vanilla.vertex.formats.glyph.GlyphVertexSink;
import net.caffeinemc.sodium.interop.vanilla.vertex.formats.glyph.writer.GlyphVertexWriterFallback;
import net.caffeinemc.sodium.render.vertex.buffer.VertexBufferView;
import net.caffeinemc.sodium.render.vertex.type.BlittableVertexType;
import net.coderbot.iris.vertices.IrisVertexFormats;

public class ExtendedGlyphVertexType implements VanillaVertexType<GlyphVertexSink>, BlittableVertexType<GlyphVertexSink> {
	public static final ExtendedGlyphVertexType INSTANCE = new ExtendedGlyphVertexType();

	@Override
	public GlyphVertexSink createFallbackWriter(VertexConsumer vertexConsumer) {
		return new GlyphVertexWriterFallback(vertexConsumer);
	}

	@Override
	public GlyphVertexSink createBufferWriter(VertexBufferView buffer, boolean direct) {
		return direct ? new GlyphVertexBufferWriterUnsafe(buffer) : new GlyphVertexBufferWriterNio(buffer);
	}

	@Override
	public VertexFormat getVertexFormat() {
		return IrisVertexFormats.TERRAIN;
	}

	public BlittableVertexType<GlyphVertexSink> asBlittable() {
		return this;
	}
}
