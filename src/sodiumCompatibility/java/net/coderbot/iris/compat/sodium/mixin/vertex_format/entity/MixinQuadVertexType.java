package net.coderbot.iris.compat.sodium.mixin.vertex_format.entity;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import me.jellysquid.mods.sodium.client.model.vertex.buffer.VertexBufferView;
import me.jellysquid.mods.sodium.client.model.vertex.formats.quad.QuadVertexSink;
import me.jellysquid.mods.sodium.client.model.vertex.formats.quad.QuadVertexType;
import me.jellysquid.mods.sodium.client.model.vertex.formats.quad.writer.QuadVertexBufferWriterNio;
import me.jellysquid.mods.sodium.client.model.vertex.formats.quad.writer.QuadVertexBufferWriterUnsafe;
import net.coderbot.iris.block_rendering.BlockRenderingSettings;
import net.coderbot.iris.compat.sodium.impl.vertex_format.entity_xhfp.EntityVertexBufferWriterNio;
import net.coderbot.iris.compat.sodium.impl.vertex_format.entity_xhfp.EntityVertexBufferWriterUnsafe;
import net.coderbot.iris.vertices.IrisVertexFormats;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(QuadVertexType.class)
public class MixinQuadVertexType {
	/**
	 * @author IMS
	 * @reason Redirects don't work here
	 */
	@Overwrite(remap = false)
	public QuadVertexSink createBufferWriter(VertexBufferView buffer, boolean direct) {
		if (BlockRenderingSettings.INSTANCE.shouldUseExtendedVertexFormat()) {
			return direct ? new EntityVertexBufferWriterUnsafe(buffer) : new EntityVertexBufferWriterNio(buffer);
		} else {
			return direct ? new QuadVertexBufferWriterUnsafe(buffer) : new QuadVertexBufferWriterNio(buffer);
		}
	}

	/**
	 * @author IMS
	 * @reason Cheap redirect
	 */
	@Overwrite
	public VertexFormat getVertexFormat() {
		return BlockRenderingSettings.INSTANCE.shouldUseExtendedVertexFormat()
			? IrisVertexFormats.ENTITY : DefaultVertexFormat.NEW_ENTITY;
	}
}
