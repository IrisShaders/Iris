package net.irisshaders.iris.compat.dh;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.pipeline.ColorTargetState;
import com.mojang.renderpearl.api.pipeline.CompareOp;
import com.mojang.renderpearl.api.pipeline.DepthStencilState;
import com.mojang.renderpearl.api.pipeline.IndexType;
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.renderpearl.api.vertex.VertexFormat;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.backend.FramebufferDescription;
import net.irisshaders.iris.backend.GraphicsPipeline;
import net.irisshaders.iris.backend.GraphicsShader;
import net.irisshaders.iris.backend.PipelineState;
import net.irisshaders.iris.gl.blending.BlendMode;
import net.irisshaders.iris.gl.blending.BlendModeFunction;
import net.irisshaders.iris.gl.sampler.GlSampler;
import net.irisshaders.iris.gl.sampler.SamplerHolder.TextureBinding;
import net.irisshaders.iris.gl.uniform.UniformType;
import net.irisshaders.iris.pipeline.IrisRenderingPipeline;
import net.irisshaders.iris.pipeline.transform.PatchShaderType;
import net.irisshaders.iris.pipeline.transform.TransformPatcher;
import net.irisshaders.iris.pipeline.transform.UniformPlan;
import net.irisshaders.iris.pipeline.transform.WorldShaderTransformer;
import net.irisshaders.iris.pipeline.transform.vulkan.VulkanShaderPatcher;
import net.irisshaders.iris.samplers.ProgramSamplers;
import net.irisshaders.iris.shaderpack.programs.ProgramSource;
import net.irisshaders.iris.shaderpack.texture.TextureStage;
import net.irisshaders.iris.targets.ColorAttachmentPlan;
import net.irisshaders.iris.targets.RenderFramebuffer;
import net.irisshaders.iris.targets.WorldRenderTargets;
import net.irisshaders.iris.uniforms.CommonUniforms;
import net.irisshaders.iris.uniforms.buffer.DrawUniforms;
import net.irisshaders.iris.vertices.ImmediateState;
import net.minecraft.client.Minecraft;
import org.joml.Vector2i;
import org.lwjgl.system.MemoryUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class IrisDhProgram implements AutoCloseable {
	private final DHCompatInternal compat;
	private final RenderFramebuffer framebuffer;
	private final DrawUniforms uniforms;
	private final ProgramSamplers resources;
	private final VertexFormat vertexFormat;
	private final Map<PatchShaderType, String> sources;
	private final ColorAttachmentPlan.Program outputs;
	private final ProgramSource source;
	private final String name;
	private final boolean generic;
	private final boolean shadow;
	private final boolean translucent;
	private GraphicsShader shader;
	private GraphicsPipeline pipeline;
	private GpuBuffer zeroVertices;

	public IrisDhProgram(DHCompatInternal compat, IrisRenderingPipeline parent, ProgramSource source, boolean generic, boolean shadow,
		boolean translucent) {
		this.compat = compat;
		this.source = source;
		this.generic = generic;
		this.shadow = shadow;
		this.translucent = translucent;
		this.vertexFormat = generic ? VertexFormat.builder(0).addAttribute("vPosition", GpuFormat.RGB32_FLOAT)
			.addAttribute("iris_color", GpuFormat.RGBA8_UNORM).addAttribute("aMaterial", GpuFormat.R8_SINT)
			.addAttribute("padding", GpuFormat.RGB8_UINT).build() : VertexFormat.builder(0).addAttribute("vPosition", GpuFormat.RGBA16_UINT)
			.addAttribute("iris_color", GpuFormat.RGBA8_UNORM).addAttribute("irisExtra", GpuFormat.RGBA8_UINT).build();
		String name = source.getName() + (generic ? "_generic" : "");
		var sources = generic ? TransformPatcher.patchDHGeneric(name, source.getVertexSource().orElseThrow(),
			source.getTessControlSource().orElse(null), source.getTessEvalSource().orElse(null), source.getGeometrySource().orElse(null),
			source.getFragmentSource().orElseThrow(), parent.getTextureMap(),
			parent.getTextureOverrides(TextureStage.GBUFFERS_AND_SHADOW)) : TransformPatcher.patchDHTerrain(name,
			source.getVertexSource().orElseThrow(), source.getTessControlSource().orElse(null), source.getTessEvalSource().orElse(null),
			source.getGeometrySource().orElse(null), source.getFragmentSource().orElseThrow(), parent.getTextureMap(),
			parent.getTextureOverrides(TextureStage.GBUFFERS_AND_SHADOW));
		this.name = name;
		sources = WorldShaderTransformer.patchDhUniforms(sources, shadow);
		sources = WorldShaderTransformer.vertexInputs(sources, WorldShaderTransformer.availableFormats(List.of(this.vertexFormat)));
		this.sources = sources;
		var layout = VulkanShaderPatcher.discover(sources).layout();
		this.uniforms = new DrawUniforms(layout, UniformPlan.Profile.DH);
		try {
			this.uniforms.useFrameUniforms(parent.getFrameUniforms());
			CommonUniforms.addDrawUniforms(this.uniforms);
			this.uniforms.drawUniform("modelOffset", UniformType.VEC3);
			this.uniforms.drawUniform("uSkyLight", UniformType.INT);
			this.uniforms.drawUniform("uBlockLight", UniformType.INT);

			this.resources = new ProgramSamplers(parent.getPackSamplers(), layout);
			this.resources.setExternalBindings(
				unit -> new TextureBinding("dh/lightmap", () -> Minecraft.getInstance().gameRenderer.levelLightmap(), () -> GlSampler.NEAREST));
			parent.addGbufferOrShadowSamplers(this.resources, this.resources,
				shadow ? parent::getFlippedBeforeShadow : translucent ? parent::getFlippedAfterTranslucent : parent::getFlippedAfterPrepare,
				shadow, false, true, false, shadow ? WorldRenderTargets.Phase.SHADOW
					: translucent ? WorldRenderTargets.Phase.TRANSLUCENT
					: WorldRenderTargets.Phase.OPAQUE);
			this.resources.addTextureSampler(new TextureBinding("dh/blockAtlas", this.compat::getBlockAtlas, () -> GlSampler.MIPPED_NEAREST), false,
				"dhBlockAtlas");
			this.resources.finish();

			int limit = RenderSystem.getDevice().getDeviceInfo().limits().maxColorAttachments();
			this.outputs = new ColorAttachmentPlan.Builder().program(name, source.getDirectives(), Set.of()).build(limit).program(name);
			this.framebuffer = shadow ? parent.createDHFramebufferShadow(source) : parent.createDHFramebuffer(source, translucent);
			if (!shadow) {
				this.framebuffer.addDepthAttachment(this.compat.getDepthTexture(false).texture());
			}
		} catch (RuntimeException | Error failure) {
			this.uniforms.close();
			throw failure;
		}
	}

	public void prepare() {
		if (this.shader != null) {
			return;
		}
		GraphicsShader shader = null;
		try {
			var patched = TransformPatcher.patchProgram(this.name, this.sources, this.resources, this.uniforms, this.outputs);
			shader = Iris.getRenderBackend().createGraphicsShader(this.name, patched, this.resources, this.uniforms);
			this.uniforms.resolveLayout(shader.getSource().layout());
			var fixedState = RenderPipeline.builder().withLocation("iris/" + this.name).withVertexShader("core/screenquad")
				.withFragmentShader("core/blit_screen").withVertexBinding(0, this.vertexFormat)
				.withPrimitiveTopology(PrimitiveTopology.TRIANGLES).withCull(!this.translucent && !this.generic)
				.withColorTargetState(ColorTargetState.DEFAULT).withDepthStencilState(new DepthStencilState(CompareOp.GREATER_THAN, true))
				.build();
			var description = new FramebufferDescription(this.framebuffer.getColorViews(),
				this.framebuffer.getDepthView().texture().getFormat());
			BlendMode blend = this.translucent || this.generic ? new BlendMode(BlendModeFunction.SRC_ALPHA.getGlId(),
				BlendModeFunction.ONE_MINUS_SRC_ALPHA.getGlId(), BlendModeFunction.ONE.getGlId(),
				BlendModeFunction.ONE_MINUS_SRC_ALPHA.getGlId()) : null;
			var attributes = WorldShaderTransformer.attributes(shader.getSource().sources().get(PatchShaderType.VERTEX),
				List.of(this.vertexFormat));
			if (attributes.stream().anyMatch(attribute -> attribute.binding() == 1)) {
				var data = MemoryUtil.memAlloc(32);
				try {
					MemoryUtil.memSet(data, 0);
					this.zeroVertices = RenderSystem.getDevice()
						.createBuffer(() -> "DH zero vertex attributes", GpuBuffer.USAGE_VERTEX, data);
				} finally {
					MemoryUtil.memFree(data);
				}
			}
			this.pipeline = shader.createPipeline(
				new PipelineState(description, this.outputs.blends(this.source.getDirectives(), blend), attributes,
					this.vertexFormat.getVertexSize(), fixedState, this.shadow, this.zeroVertices));
			this.shader = shader;
		} catch (RuntimeException | Error failure) {
			this.framebuffer.destroy();
			if (shader != null) {
				shader.close();
			}
			throw failure;
		}
	}

	public RenderFramebuffer getFramebuffer() {
		return this.framebuffer;
	}

	public Batch beginBatch() {
		return new Batch();
	}

	public void draw(GpuBuffer vertices, GpuBuffer indices, IndexType indexType, int count) {
		try (var batch = this.beginBatch()) {
			batch.draw(vertices, indices, indexType, count);
		}
	}

	private void drawBatch(List<Draw> draws) {
		if (draws.isEmpty()) {
			return;
		}
		CommonUniforms.writeDrawUniforms(this.uniforms);
		var backend = Iris.getRenderBackend();
		var colors = this.framebuffer.getColorViews();
		var depth = this.framebuffer.getDepthView();
		boolean ignore = ImmediateState.temporarilyIgnorePass;
		ImmediateState.temporarilyIgnorePass = true;
		try (var pass = backend.createRenderPass(this.name, this.pipeline.framebuffer(), colors, depth,
			new Vector2i(depth.getWidth(0), depth.getHeight(0)))) {
			for (var draw : draws) {
				this.uniforms.set3f("modelOffset", draw.offsetX, draw.offsetY, draw.offsetZ);
				this.uniforms.set1i("uSkyLight", draw.skyLight);
				this.uniforms.set1i("uBlockLight", draw.blockLight);
				pass.setVertexBuffer(0, draw.vertices.slice());
				pass.setIndexBuffer(draw.indices, draw.indexType);
				pass.bindPipeline(this.pipeline);
				pass.drawIndexed(draw.count, 1, 0, 0, 0);
			}
		} finally {
			ImmediateState.temporarilyIgnorePass = ignore;
		}
	}

	public class Batch implements AutoCloseable {
		private final List<Draw> draws = new ArrayList<>();

		public void draw(GpuBuffer vertices, GpuBuffer indices, IndexType indexType, int count) {
			if (count == 0 || vertices == null || indices == null) {
				return;
			}
			var compat = IrisDhProgram.this.compat;
			var offset = compat.getModelOffset();
			this.draws.add(new Draw(vertices, indices, indexType, count, offset.x, offset.y, offset.z,
				compat.getSkyLight(), compat.getBlockLight()));
		}

		@Override
		public void close() {
			try {
				IrisDhProgram.this.drawBatch(this.draws);
			} finally {
				this.draws.clear();
			}
		}
	}

	public record Draw(GpuBuffer vertices, GpuBuffer indices, IndexType indexType, int count,
		float offsetX, float offsetY, float offsetZ, int skyLight, int blockLight) {
	}

	@Override
	public void close() {
		this.uniforms.close();
		this.framebuffer.destroy();
		if (this.pipeline != null) {
			Iris.getRenderBackend().retire(this.pipeline::close);
		}
		if (this.shader != null) {
			Iris.getRenderBackend().retire(this.shader::close);
		}
		if (this.zeroVertices != null) {
			this.zeroVertices.close();
		}
	}
}
