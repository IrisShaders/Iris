package net.irisshaders.iris.pathways;

import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import com.mojang.renderpearl.backend.opengl.GlBuffer;
import com.mojang.renderpearl.backend.opengl.GlDevice;
import com.mojang.renderpearl.backend.opengl.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.renderpearl.api.vertex.VertexFormat;
import net.irisshaders.iris.gl.IrisRenderSystem;
import net.irisshaders.iris.helpers.VertexBufferHelper;
import net.irisshaders.iris.mixin.GpuDeviceAccessor;
import org.lwjgl.opengl.GL32;
import org.lwjgl.opengl.GL46C;

/**
 * Renders a full-screen textured quad to the screen. Used in composite / deferred rendering.
 */
public class FullScreenQuadRenderer {
	public static final FullScreenQuadRenderer INSTANCE = new FullScreenQuadRenderer();

	private final GpuBuffer quad;

	private FullScreenQuadRenderer() {
		var x = new ByteBufferBuilder(64);
		BufferBuilder bufferBuilder = new BufferBuilder(x, PrimitiveTopology.QUADS, DefaultVertexFormat.POSITION_TEX);
		bufferBuilder.addVertex(0.0F, 0.0F, 0.0F).setUv(0.0F, 0.0F);
		bufferBuilder.addVertex(1.0F, 0.0F, 0.0F).setUv(1.0F, 0.0F);
		bufferBuilder.addVertex(1.0F, 1.0F, 0.0F).setUv(1.0F, 1.0F);
		bufferBuilder.addVertex(0.0F, 1.0F, 0.0F).setUv(0.0F, 1.0F);
		MeshData meshData = bufferBuilder.build();

		quad = RenderSystem.getDevice().createBuffer(() -> "Quad", GpuBuffer.USAGE_COPY_DST | GpuBuffer.USAGE_VERTEX, meshData.vertexBuffer());
		meshData.close();
		x.close();
	}

	public static int init() {
		return -1;
	}

	public GpuBuffer getQuad() {
		return quad;
	}

	public void bind() {
		((GlDevice) ((GpuDeviceAccessor) RenderSystem.getDevice()).getBackend()).vertexArrayCache().bindVertexArray(new VertexFormat[] { DefaultVertexFormat.POSITION_TEX }, new GpuBufferSlice[] { quad.slice() }, null);
	}
}
