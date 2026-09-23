package net.irisshaders.iris.compat.dh;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
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
import net.irisshaders.iris.backend.SyncCommand;
import net.irisshaders.iris.gl.blending.BlendMode;
import net.irisshaders.iris.gl.blending.BlendModeFunction;
import net.irisshaders.iris.gl.sampler.GlSampler;
import net.irisshaders.iris.gl.sampler.SamplerHolder.TextureBinding;
import net.irisshaders.iris.gl.state.FogMode;
import net.irisshaders.iris.gl.uniform.UniformUpdateFrequency;
import net.irisshaders.iris.pipeline.IrisRenderingPipeline;
import net.irisshaders.iris.pipeline.transform.PatchShaderType;
import net.irisshaders.iris.pipeline.transform.ShaderResourceLayout;
import net.irisshaders.iris.pipeline.transform.TransformPatcher;
import net.irisshaders.iris.pipeline.transform.WorldShaderTransformer;
import net.irisshaders.iris.pipeline.transform.vulkan.VulkanShaderPatcher;
import net.irisshaders.iris.samplers.ProgramResources;
import net.irisshaders.iris.shaderpack.programs.ProgramSource;
import net.irisshaders.iris.shaderpack.texture.TextureStage;
import net.irisshaders.iris.targets.ColorAttachmentPlan;
import net.irisshaders.iris.targets.RenderFramebuffer;
import net.irisshaders.iris.uniforms.CommonUniforms;
import net.irisshaders.iris.uniforms.SystemTimeUniforms;
import net.irisshaders.iris.uniforms.buffer.BufferUniformHolder;
import net.irisshaders.iris.vertices.ImmediateState;
import net.minecraft.client.Minecraft;
import org.joml.Vector2i;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class IrisDhProgram implements AutoCloseable {
	private final DHCompatInternal compat;
	private final RenderFramebuffer framebuffer;
	private final BufferUniformHolder uniforms;
	private final ProgramResources resources;
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
	private Map<String, List<GpuBufferSlice>> uniformBuffers = Map.of();

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
		sources = WorldShaderTransformer.vertexInputs(sources, WorldShaderTransformer.availableFormats(List.of(this.vertexFormat)));
		this.sources = sources;
		var layout = VulkanShaderPatcher.discover(sources).layout();
		this.uniforms = new BufferUniformHolder(layout);
		this.uniforms.useFrameUniforms(parent.getFrameUniforms());
		CommonUniforms.addDynamicUniforms(this.uniforms, FogMode.PER_VERTEX);
		this.uniforms.uniform3f(UniformUpdateFrequency.CUSTOM, "modelOffset", () -> this.compat.getModelOffset());
		this.uniforms.uniformMatrix(UniformUpdateFrequency.CUSTOM, "iris_ModelViewMatrix", () -> this.compat.getModelView());
		this.uniforms.uniformMatrix(UniformUpdateFrequency.CUSTOM, "iris_ModelViewMatrixInverse", () -> this.compat.getModelViewInverse());
		this.uniforms.uniformMatrix(UniformUpdateFrequency.CUSTOM, "iris_ProjectionMatrix", () -> this.compat.getProjection());
		this.uniforms.uniformMatrix(UniformUpdateFrequency.CUSTOM, "iris_ProjectionMatrixInverse",
			() -> this.compat.getProjectionInverse());
		this.uniforms.uniformMatrix3(UniformUpdateFrequency.CUSTOM, "iris_NormalMatrix", () -> this.compat.getNormalMatrix());
		this.uniforms.uniform1f(UniformUpdateFrequency.ONCE, "mircoOffset", () -> 0.01f);
		this.uniforms.uniform1f(UniformUpdateFrequency.CUSTOM, "clipDistance", () -> this.compat.getClipDistance());
		this.uniforms.uniform1i(UniformUpdateFrequency.CUSTOM, "uSkyLight", () -> this.compat.getSkyLight());
		this.uniforms.uniform1i(UniformUpdateFrequency.CUSTOM, "uBlockLight", () -> this.compat.getBlockLight());

		this.resources = new ProgramResources(parent.getPackSamplers(), layout);
		this.resources.addStorageBuffers(parent.getStorageBuffers());
		this.resources.setUniformBuffers(() -> this.uniformBuffers);
		this.resources.setExternalBindings(
			unit -> new TextureBinding("dh/lightmap", () -> Minecraft.getInstance().gameRenderer.levelLightmap(), () -> GlSampler.NEAREST));
		parent.addGbufferOrShadowSamplers(this.resources, this.resources,
			shadow ? parent::getFlippedBeforeShadow : translucent ? parent::getFlippedAfterTranslucent : parent::getFlippedAfterPrepare,
			shadow, false, true, false);
		this.resources.addTextureSampler(new TextureBinding("dh/blockAtlas", this.compat::getBlockAtlas, () -> GlSampler.NEAREST), false,
			"dhBlockAtlas");
		this.resources.finish();

		int limit = RenderSystem.getDevice().getDeviceInfo().limits().maxColorAttachments();
		this.outputs = new ColorAttachmentPlan.Builder().program(name, source.getDirectives(), Set.of()).build(limit).program(name);
		this.framebuffer = shadow ? parent.createDHFramebufferShadow(source) : parent.createDHFramebuffer(source, translucent);
		if (!shadow) {
			this.framebuffer.addDepthAttachment(this.compat.getDepthTexture(false).texture());
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
			var description = new FramebufferDescription(
				this.framebuffer.getColorViews().stream().map(view -> view.texture().getFormat()).toList(),
				this.framebuffer.getDepthView().texture().getFormat());
			BlendMode blend = this.translucent || this.generic ? new BlendMode(BlendModeFunction.SRC_ALPHA.getGlId(),
				BlendModeFunction.ONE_MINUS_SRC_ALPHA.getGlId(), BlendModeFunction.ONE.getGlId(),
				BlendModeFunction.ONE_MINUS_SRC_ALPHA.getGlId()) : null;
			var attributes = WorldShaderTransformer.attributes(shader.getSource().sources().get(PatchShaderType.VERTEX),
				List.of(this.vertexFormat));
			if (attributes.stream().anyMatch(attribute -> attribute.binding() == 1)) {
				this.zeroVertices = RenderSystem.getDevice()
					.createBuffer(() -> "DH zero vertex attributes", GpuBuffer.USAGE_VERTEX, ByteBuffer.allocateDirect(32));
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

	public void draw(GpuBuffer vertices, GpuBuffer indices, IndexType indexType, int count) {
		if (count == 0 || vertices == null || indices == null) {
			return;
		}
		var level = Minecraft.getInstance().level;
		this.uniforms.update(level == null ? 0 : level.getGameTime(), SystemTimeUniforms.COUNTER.getAsInt());
		int alignment = RenderSystem.getDevice().getDeviceInfo().limits().minUniformOffsetAlignment();
		this.uniformBuffers = this.uniforms.data().hasRemaining() ? Map.of(ShaderResourceLayout.UNIFORM_BLOCK,
			List.of(this.uniforms.upload(RenderSystem.getDevice().createCommandEncoder(), alignment))) : Map.of();
		var backend = Iris.getRenderBackend();
		var sync = backend.createSync().read(this.pipeline).read(vertices, SyncCommand.Access.VERTEX)
			.read(indices, SyncCommand.Access.INDEX);
		var colors = this.framebuffer.getColorViews();
		var depth = this.framebuffer.getDepthView();
		colors.forEach(view -> sync.write(view, SyncCommand.Access.COLOR_ATTACHMENT));
		sync.write(depth, SyncCommand.Access.DEPTH_ATTACHMENT).nextIs(SyncCommand.Stage.GRAPHICS).run();
		boolean ignore = ImmediateState.temporarilyIgnorePass;
		ImmediateState.temporarilyIgnorePass = true;
		try (var pass = backend.createRenderPass(this.name, this.pipeline.framebuffer(), colors, depth,
			new Vector2i(depth.getWidth(0), depth.getHeight(0)))) {
			pass.setVertexBuffer(0, vertices.slice());
			pass.setIndexBuffer(indices, indexType);
			pass.bindPipeline(this.pipeline);
			pass.drawIndexed(count, 1, 0, 0, 0);
		} finally {
			ImmediateState.temporarilyIgnorePass = ignore;
			this.uniformBuffers = Map.of();
		}
	}

	@Override
	public void close() {
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
