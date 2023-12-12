package net.coderbot.iris.compat.sodium.impl.vertex_format.terrain_xhfp;

import me.jellysquid.mods.sodium.client.render.chunk.vertex.format.ModelQuadEncoder;
import me.jellysquid.mods.sodium.client.render.chunk.vertex.format.ModelQuadFormat;
import net.caffeinemc.mods.sodium.api.util.ColorABGR;
import net.coderbot.iris.compat.sodium.impl.block_context.BlockContextHolder;
import net.coderbot.iris.compat.sodium.impl.block_context.ContextAwareVertexWriter;
import net.coderbot.iris.vertices.NormI8;
import net.coderbot.iris.vertices.NormalHelper;
import org.lwjgl.system.MemoryUtil;

public class XHFPModelVertexType implements ModelQuadFormat {

	public static final int STRIDE = 96;

	@Override
	public ModelQuadEncoder getEncoder() {
		return new XHFPEncoder();
	}

	@Override
	public int getStride() {
		return STRIDE;
	}
}
