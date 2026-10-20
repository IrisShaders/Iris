package net.irisshaders.iris.opengl;

import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import com.mojang.renderpearl.api.pipeline.DepthStencilState;
import com.mojang.renderpearl.api.vertex.VertexFormat;
import com.mojang.renderpearl.backend.opengl.GlBuffer;
import com.mojang.renderpearl.backend.opengl.GlConst;
import com.mojang.renderpearl.backend.opengl.GlStateManager;
import net.irisshaders.iris.backend.FramebufferDescription;
import net.irisshaders.iris.backend.GraphicsPipeline;
import net.irisshaders.iris.backend.GraphicsShader;
import net.irisshaders.iris.backend.PipelineState;
import net.irisshaders.iris.gl.IrisRenderSystem;
import net.irisshaders.iris.gl.blending.BlendMode;
import net.irisshaders.iris.pipeline.transform.PatchShaderType;
import net.irisshaders.iris.pipeline.transform.PatchedShader;
import net.irisshaders.iris.pipeline.transform.WorldShaderTransformer;
import net.irisshaders.iris.samplers.ProgramSamplers;
import net.irisshaders.iris.uniforms.buffer.DrawUniforms;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL14C;
import org.lwjgl.opengl.GL15C;
import org.lwjgl.opengl.GL20C;
import org.lwjgl.opengl.GL30C;
import org.lwjgl.opengl.GL33C;
import org.lwjgl.opengl.GL40C;

import java.util.List;

public class GlGraphicsShader implements GraphicsShader {
	private final GlShaderBindings shader;
	private final ProgramSamplers resources;
	private final DrawUniforms uniforms;
	private boolean closed;

	public GlGraphicsShader(GlShaderBindings shader, ProgramSamplers resources, DrawUniforms uniforms) {
		this.shader = shader.retain();
		this.resources = resources;
		this.uniforms = uniforms;
	}

	@Override
	public PatchedShader getSource() {
		return this.shader.source;
	}

	@Override
	public GraphicsPipeline createPipeline(PipelineState state) {
		if (this.closed) {
			throw new IllegalStateException("shader closed");
		}

		return new Pipeline(state);
	}

	@Override
	public void close() {
		if (!this.closed) {
			this.closed = true;
			this.shader.close();
		}
	}

	public class Pipeline implements GraphicsPipeline {
		private final PipelineState state;
		private final DepthStencilState depth;
		private final boolean cull;
		private final int polygonMode;
		private final int topology;
		private final int[] colorMasks;
		private final int[] drawBuffers;
		private final int[] defaultDrawBuffers;
		private final BlendMode[] blends;
		private final List<VertexFormat> vertexFormats;
		private final GpuBufferSlice[] boundVertices = new GpuBufferSlice[16];
		private int vao;
		private boolean vertexStateInitialized;

		public Pipeline(PipelineState state) {
			this.state = state;
			this.vertexFormats = state.vertexFormats();
			this.depth = state.depthState();
			this.cull = PipelineState.cull(state.vanillaPipeline(), state.shadowPass());
			this.polygonMode = state.vanillaPipeline() == null ? GL11C.GL_FILL : GlConst.toGl(state.vanillaPipeline().getPolygonMode());
			this.topology = GlGraphicsShader.this.shader.source.sources().containsKey(
				PatchShaderType.TESS_CONTROL) ? GL40C.GL_PATCHES : state.vanillaPipeline() == null ? GL11C.GL_TRIANGLES : GlConst.toGl(
				state.vanillaPipeline().getPrimitiveTopology());
			var outputs = state.outputLocations();
			this.colorMasks = new int[outputs == null ? state.colorAttachmentCount() : outputs.size()];
			this.drawBuffers = new int[this.colorMasks.length];
			this.defaultDrawBuffers = outputs == null ? this.drawBuffers : new int[state.colorAttachmentCount()];
			for (int i = 0; i < this.defaultDrawBuffers.length; i++) {
				this.defaultDrawBuffers[i] = GL30C.GL_COLOR_ATTACHMENT0 + i;
			}
			this.blends = new BlendMode[this.colorMasks.length];
			var written = GlGraphicsShader.this.shader.source.writtenAttachments();

			for (int i = 0; i < this.colorMasks.length; i++) {
				int attachment = outputs == null ? i : outputs.get(i);
				this.drawBuffers[i] = GL30C.GL_COLOR_ATTACHMENT0 + attachment;
				this.colorMasks[i] = PipelineState.colorMask(state.vanillaPipeline(), written == null || written.get(i));
				this.blends[i] = this.colorMasks[i] == 0 || PipelineState.isInteger(state.colorFormats()[attachment]) ? null : state.blendModes()
					.get(attachment);
			}

			if (!GlPipelineState.supportsIndexedBlend()) {
				BlendMode previous = null;

				for (var blend : this.blends) {
					if (blend == null) {
						continue;
					}

					if (previous != null && !previous.equals(blend)) {
						throw new UnsupportedOperationException("independent attachment blending requires GL_ARB_draw_buffers_blend");
					}

					previous = blend;
				}
			}

			this.vao = GlStateManager._glGenVertexArrays();
		}

		public int getTopology() {
			return this.topology;
		}

		public void bind() {
			if (this.vao == 0) {
				throw new IllegalStateException("pipeline closed");
			}

			GlGraphicsShader.this.shader.bind(GlGraphicsShader.this.resources, GlGraphicsShader.this.uniforms);
			this.applyState();
		}

		public void bindVertices(GpuBufferSlice[] vertices) {
			GlStateManager._glBindVertexArray(this.vao);

			for (var attribute : this.state.vertexAttributes()) {
				int slot = attribute.binding();
				int location = attribute.location();
				var format = attribute.format();
				var vertex = slot < vertices.length ? vertices[slot] : null;
				boolean fallback = this.state.vanillaPipeline() != null && slot == WorldShaderTransformer.fallbackVertexBinding(
					this.vertexFormats);

				if (fallback && this.state.zeroVertexBuffer() != null) {
					if (!this.vertexStateInitialized) {
						GL20C.glDisableVertexAttribArray(location);
					}

					float x = 0, y = 0, z = 0, w = 0;
					switch (attribute.name() == null ? "" : attribute.name()) {
						case "iris_UV0" -> { x = 0.5f; y = 0.5f; z = 0.5f; w = 0.5f; }
						case "iris_UV2" -> { x = 240; y = 240; z = 240; w = 240; }
						case "iris_Color" -> { x = 1; y = 1; z = 1; w = 1; }
						case "iris_Normal" -> z = 1;
						case "iris_Entity", "mc_Entity", "at_tangent", "mc_midTexCoord", "at_midBlock" -> {
							if (format.componentCount() == 4) w = 1;
						}
					}
					if (format.componentType().name().startsWith("UINT")) {
						GL30C.glVertexAttribI4ui(location, (int) x, (int) y, (int) z, (int) w);
					} else if (format.componentType().name().startsWith("SINT")) {
						GL30C.glVertexAttribI4i(location, (int) x, (int) y, (int) z, (int) w);
					} else {
						IrisRenderSystem.vertexAttrib4f(location, x, y, z, w);
					}

					continue;
				}

				if (vertex == null) {
					throw new IllegalStateException("missing vertex buffer " + slot);
				}

				if (!this.vertexStateInitialized) {
					GlStateManager._enableVertexAttribArray(location);
					GL33C.glVertexAttribDivisor(location,
						this.state.vanillaPipeline() == null ? 0 : this.vertexFormats.get(slot).getStepRate());
				}

				var previous = this.boundVertices[slot];

				if (previous != null && previous.buffer() == vertex.buffer() && previous.offset() == vertex.offset()) {
					continue;
				}

				GlStateManager._glBindBuffer(GL15C.GL_ARRAY_BUFFER, ((GlBuffer) vertex.buffer()).handle());
				int stride = this.state.vanillaPipeline() == null ? this.state.vertexStride() : this.vertexFormats.get(slot)
					.getVertexSize();
				int type = GlConst.toGlType(format);
				long offset = vertex.offset() + attribute.offset();

				if (GlConst.isGlFormatInteger(GlConst.toGlExternalId(format))) {
					GlStateManager._vertexAttribIPointer(location, format.componentCount(), type, stride, offset);
				} else {
					GlStateManager._vertexAttribPointer(location, format.componentCount(), type, GlConst.isFormatNormalized(format), stride,
						offset);
				}
			}

			System.arraycopy(vertices, 0, this.boundVertices, 0, Math.min(vertices.length, this.boundVertices.length));
			this.vertexStateInitialized = true;
		}

		public void restoreDrawBuffers(int framebuffer) {
			int previous = GlStateManager.getFrameBuffer(GL30C.GL_DRAW_FRAMEBUFFER);
			GlStateManager._glBindFramebuffer(GL30C.GL_DRAW_FRAMEBUFFER, framebuffer);
			GL20C.glDrawBuffers(this.defaultDrawBuffers);
			GlStateManager._glBindFramebuffer(GL30C.GL_DRAW_FRAMEBUFFER, previous);
		}

		private void applyState() {
			IrisRenderSystem.drawBuffers(GlStateManager.getFrameBuffer(GL30C.GL_DRAW_FRAMEBUFFER), this.drawBuffers);
			if (this.depth == null) {
				GlStateManager._disableDepthTest();
				GlStateManager._depthMask(false);
				GlStateManager._disablePolygonOffset();
			} else {
				GlStateManager._enableDepthTest();
				GlStateManager._depthFunc(GlConst.toGl(this.depth.depthTest()));
				GlStateManager._depthMask(this.depth.writeDepth());

				if (this.depth.depthBiasConstant() != 0 || this.depth.depthBiasScaleFactor() != 0) {
					GlStateManager._polygonOffset(this.depth.depthBiasScaleFactor(), this.depth.depthBiasConstant());
					GlStateManager._enablePolygonOffset();
				} else {
					GlStateManager._disablePolygonOffset();
				}
			}

			if (this.cull) {
				GlStateManager._enableCull();
			} else {
				GlStateManager._disableCull();
			}

			GlStateManager._polygonMode(GL11C.GL_FRONT_AND_BACK, this.polygonMode);

			if (this.getTopology() == GL40C.GL_PATCHES) {
				GL40C.glPatchParameteri(GL40C.GL_PATCH_VERTICES, 3);
			}

			GlStateManager._blendEquationSeparate(GL14C.GL_FUNC_ADD, GL14C.GL_FUNC_ADD);
			for (int i = 0; i < this.colorMasks.length; i++) {
				GlStateManager._colorMask(i, this.colorMasks[i]);
				var blend = this.blends[i];

				if (blend == null) {
					GlStateManager._disableBlend(i);
				} else {
					GlStateManager._enableBlend(i);
					GlPipelineState.blendFunction(i, blend);
				}
			}
		}

		@Override
		public FramebufferDescription framebuffer() {
			return this.state.framebuffer();
		}

		public boolean needsDrawBarrier() {
			return this.state.vanillaPipeline() != null && GlGraphicsShader.this.shader.hasStorageWrites();
		}

		public void pushConstants(GpuBufferSlice data) {
			GlGraphicsShader.this.shader.bindPushConstants(data);
		}

		@Override
		public void close() {
			if (this.vao != 0) {
				GL30C.glDeleteVertexArrays(this.vao);
				this.vao = 0;
			}
		}
	}
}
