package net.coderbot.iris.compat.sodium.mixin.vertex_format.replacements;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import me.jellysquid.mods.sodium.client.model.vertex.buffer.VertexBufferView;
import me.jellysquid.mods.sodium.client.model.vertex.formats.particle.ParticleVertexSink;
import me.jellysquid.mods.sodium.client.model.vertex.formats.particle.ParticleVertexType;
import me.jellysquid.mods.sodium.client.model.vertex.formats.particle.writer.ParticleVertexBufferWriterNio;
import me.jellysquid.mods.sodium.client.model.vertex.formats.particle.writer.ParticleVertexBufferWriterUnsafe;
import me.jellysquid.mods.sodium.client.model.vertex.formats.quad.QuadVertexSink;
import me.jellysquid.mods.sodium.client.model.vertex.formats.quad.QuadVertexType;
import me.jellysquid.mods.sodium.client.model.vertex.formats.quad.writer.QuadVertexBufferWriterNio;
import me.jellysquid.mods.sodium.client.model.vertex.formats.quad.writer.QuadVertexBufferWriterUnsafe;
import net.coderbot.iris.compat.sodium.impl.vertex_format.entity_xhfp.writer.EntityVertexBufferWriterNio;
import net.coderbot.iris.compat.sodium.impl.vertex_format.entity_xhfp.writer.EntityVertexBufferWriterUnsafe;
import net.coderbot.iris.compat.sodium.impl.vertex_format.particle_xhfp.writer.IrisParticleVertexBufferWriterNio;
import net.coderbot.iris.compat.sodium.impl.vertex_format.particle_xhfp.writer.IrisParticleVertexBufferWriterUnsafe;
import net.coderbot.iris.vertices.IrisVertexFormats;
import net.irisshaders.iris.api.v0.IrisApi;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(ParticleVertexType.class)
public class MixinParticleVertexType {
	/**
	 * @author IMS
	 * @reason Redirects don't work here
	 */
	@Overwrite(remap = false)
	public ParticleVertexSink createBufferWriter(VertexBufferView buffer, boolean direct) {
		if (IrisApi.getInstance().isShaderPackInUse()) {
			return direct ? new IrisParticleVertexBufferWriterUnsafe(buffer) : new IrisParticleVertexBufferWriterNio(buffer);
		} else {
			return direct ? new ParticleVertexBufferWriterUnsafe(buffer) : new ParticleVertexBufferWriterNio(buffer);
		}
	}

	/**
	 * @author IMS
	 * @reason Cheap redirect
	 */
	@Overwrite
	public VertexFormat getVertexFormat() {
		return IrisApi.getInstance().isShaderPackInUse() ? IrisVertexFormats.PARTICLE : DefaultVertexFormat.PARTICLE;
	}
}
