package net.irisshaders.iris.vertices.sodium;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.caffeinemc.mods.sodium.api.memory.MemoryIntrinsics;
import net.caffeinemc.mods.sodium.api.vertex.serializer.VertexSerializer;
import net.irisshaders.iris.uniforms.CapturedRenderingState;
import net.irisshaders.iris.vertices.IrisVertexFormats;
import org.lwjgl.system.MemoryUtil;

public class ParticleExtVertexSerializer implements VertexSerializer {
	private static final int OFFSET_ENTITY = IrisVertexFormats.PARTICLE.getElement(IrisVertexFormats.ENTITY_ATTRIBUTE).offset();
	private static final int SRC_STRIDE = DefaultVertexFormat.PARTICLE.getVertexSize();
	private static final int DST_STRIDE = IrisVertexFormats.PARTICLE.getVertexSize();

	@Override
	public void serialize(long src, long dst, int vertexCount) {
		int particle = CapturedRenderingState.INSTANCE.getCurrentRenderedParticle();

		for (int i = 0; i < vertexCount; i++) {
			MemoryIntrinsics.copyMemory(src, dst, SRC_STRIDE);

			MemoryUtil.memPutInt(dst + OFFSET_ENTITY, particle);

			src += SRC_STRIDE;
			dst += DST_STRIDE;
		}
	}
}
