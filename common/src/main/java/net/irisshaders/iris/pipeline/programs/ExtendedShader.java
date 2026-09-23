package net.irisshaders.iris.pipeline.programs;

import com.google.common.collect.ImmutableSet;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.renderpearl.api.vertex.VertexFormat;
import com.mojang.renderpearl.backend.opengl.GlConst;
import com.mojang.renderpearl.frontend.FrontendRenderPipeline;
import com.mojang.renderpearl.util.TextureViewAndSampler;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.backend.FramebufferDescription;
import net.irisshaders.iris.backend.GraphicsPipeline;
import net.irisshaders.iris.backend.GraphicsShader;
import net.irisshaders.iris.backend.IrisRenderPass;
import net.irisshaders.iris.backend.PipelineState;
import net.irisshaders.iris.gl.blending.AlphaTest;
import net.irisshaders.iris.gl.blending.BlendMode;
import net.irisshaders.iris.gl.blending.BlendModeOverride;
import net.irisshaders.iris.gl.sampler.GlSampler;
import net.irisshaders.iris.gl.sampler.SamplerHolder.TextureBinding;
import net.irisshaders.iris.gl.state.FogMode;
import net.irisshaders.iris.gl.state.ShaderAttributeInputs;
import net.irisshaders.iris.pipeline.IrisRenderPipeline;
import net.irisshaders.iris.pipeline.IrisRenderingPipeline;
import net.irisshaders.iris.pipeline.transform.PatchShaderType;
import net.irisshaders.iris.pipeline.transform.ShaderResourceLayout;
import net.irisshaders.iris.pipeline.transform.TransformPatcher;
import net.irisshaders.iris.pipeline.transform.WorldShaderTransformer;
import net.irisshaders.iris.pipeline.transform.vulkan.VulkanShaderPatcher;
import net.irisshaders.iris.samplers.ProgramResources;
import net.irisshaders.iris.shaderpack.properties.ProgramDirectives;
import net.irisshaders.iris.targets.WorldRenderTargets;
import net.irisshaders.iris.uniforms.CapturedRenderingState;
import net.irisshaders.iris.uniforms.CommonUniforms;
import net.irisshaders.iris.uniforms.buffer.BufferUniformHolder;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ExtendedShader implements AutoCloseable {
	private final IrisRenderingPipeline parent;
	private final String name;
	private final Map<PatchShaderType, String> sources;
	private final ProgramDirectives directives;
	private final int[] drawBuffers;
	private final BlendModeOverride blendOverride;
	private final AlphaTest alpha;
	private final VertexFormat vertexFormat;
	private final Map<List<VertexFormat>, Map<PatchShaderType, String>> inputSources = new HashMap<>();
	private final Map<OverrideKey, FrontendRenderPipeline> overrides = new HashMap<>();
	private final Map<WorldRenderTargets.Phase, Variant> variants = new EnumMap<>(WorldRenderTargets.Phase.class);
	private Map<String, Object> drawResources = Map.of();
	private Map<String, List<GpuBufferSlice>> uniformBuffers = Map.of();
	private boolean closed;
	private GpuBuffer zeroVertices;

	public ExtendedShader(IrisRenderingPipeline parent, String name, Map<PatchShaderType, String> sources, ProgramDirectives directives,
		int[] drawBuffers, BlendModeOverride blendOverride, AlphaTest alpha, ShaderAttributeInputs inputs, VertexFormat vertexFormat,
		ShaderKey shaderKey) {
		this.parent = parent;
		this.name = name;
		this.directives = directives;
		this.drawBuffers = drawBuffers.clone();
		this.blendOverride = blendOverride;
		this.alpha = alpha;
		this.vertexFormat = vertexFormat;

		var filtered = new EnumMap<PatchShaderType, String>(PatchShaderType.class);
		sources.forEach((stage, text) -> {
			if (text != null) {
				filtered.put(stage, text);
			}
		});
		this.sources = WorldShaderTransformer.patchMatrices(filtered);

		var layout = VulkanShaderPatcher.discover(this.sources).layout();

		for (var resource : layout.descriptors()) {
			if (resource.kind() != ShaderResourceLayout.Kind.STORAGE_BUFFER && resource.kind() != ShaderResourceLayout.Kind.SAMPLER && resource.kind() != ShaderResourceLayout.Kind.UNIFORM_BUFFER && resource.kind() != ShaderResourceLayout.Kind.IMAGE) {
				throw new UnsupportedOperationException("world resource is not implemented: " + name + ": " + resource.name());
			}
		}

		boolean shadow = shaderKey.isShadow();
		var phases = shaderKey.getRenderPhases();
		parent.getWorldTargets().register(name, drawBuffers, phases);

		for (var phase : phases) {
			var uniforms = new BufferUniformHolder(layout);
			uniforms.useFrameUniforms(parent.getFrameUniforms());
			CommonUniforms.addDynamicUniforms(uniforms, FogMode.PER_VERTEX);

			var samplers = new ProgramResources(parent.getPackSamplers(), layout);
			samplers.addStorageBuffers(parent.getStorageBuffers());
			samplers.setUniformBuffers(() -> this.uniformBuffers);
			samplers.setExternalBindings(unit -> new TextureBinding("world/texture/" + unit,
				() -> this.texture(unit) == null ? parent.getBiggerWhitePixel().getTextureView() : this.texture(unit).view(),
				() -> GlSampler.NEAREST, () -> this.texture(unit) == null ? null : this.texture(unit).sampler()));

			var flipped = ImmutableSet.copyOf(shadow ? parent.getFlippedBeforeShadow() : parent.getWorldTargets().getFlippedBuffers(phase));
			parent.addGbufferOrShadowSamplers(samplers, samplers, () -> flipped, shadow, inputs.hasTex(), inputs.hasLight(),
				inputs.hasOverlay());

			samplers.addDrawTextureSampler(new TextureBinding("world/glint",
				() -> inputs.isGlint() && inputs.hasOverlay() && this.drawResources.get(
					"GlintSampler") instanceof TextureViewAndSampler texture ? texture.view() : parent.getWhitePixel().getTextureView(),
				() -> GlSampler.NEAREST, () -> inputs.isGlint() && inputs.hasOverlay() && this.drawResources.get(
				"GlintSampler") instanceof TextureViewAndSampler texture ? texture.sampler() : null), false, "glintTexture");

			for (var descriptor : layout.descriptors()) {
				if (descriptor.kind() == ShaderResourceLayout.Kind.SAMPLER && descriptor.type().endsWith("BUFFER")) {
					samplers.addTexelSampler(descriptor.name(), "world/texel/" + descriptor.name(), () -> {
						var view = this.drawResources.get(descriptor.name());

						if (!(view instanceof Long)) {
							throw new IllegalStateException("missing world texel buffer: " + descriptor.name());
						}

						return (Long) view;
					});
				}
			}

			samplers.finish();
			this.variants.put(phase, new Variant(uniforms, samplers, layout));
		}
	}

	private TextureViewAndSampler texture(int unit) {
		var names = switch (unit) {
			case 0 -> List.of("Sampler0", "u_BlockTex", "u_MainSampler");
			case 1 -> List.of("Sampler1");
			case 2 -> List.of("Sampler2", "u_LightTex", "u_Lightmap");
			default -> throw new IllegalArgumentException("unknown world sampler unit " + unit);
		};

		for (var key : names) {
			if (this.drawResources.get(key) instanceof TextureViewAndSampler texture) {
				return texture;
			}
		}

		if (unit != 0) {
			return null;
		}

		throw new IllegalStateException("missing world texture for " + this.name + ": " + names);
	}

	public String getName() {
		return this.name;
	}

	public boolean isClosed() {
		return this.closed;
	}

	public FrontendRenderPipeline override(RenderPipeline source, FrontendRenderPipeline original) {
		if (this.closed) {
			throw new IllegalStateException("world shader is closed: " + this.name);
		}

		var formats = Collections.unmodifiableList(new ArrayList<>(source.getVertexFormatBindings()));
		return this.overrides.computeIfAbsent(new OverrideKey(source, original, formats),
			key -> new FrontendRenderPipeline(original.name(), new IrisRenderPipeline(this, source, formats), formats,
				original.uniformIndices(), original.uniforms(), original.colorTargetStates(), original.wantsDepthTexture(),
				original.pushConstantSize()));
	}

	public void apply(IrisRenderPass context, IrisRenderPipeline override, WorldRenderTargets.Phase phase, GpuFormat depthFormat,
		Map<String, Object> resources, ByteBuffer pushConstants, long tick, long frame) {
		if (this.closed) {
			throw new IllegalStateException("world program is closed: " + this.name);
		}

		var vanilla = override.source();
		var variant = Objects.requireNonNull(this.variants.get(phase), "world shader used in wrong phase");
		this.drawResources = resources;

		try {
			var key = new PipelineKey(vanilla, override.vertexFormats(), depthFormat);
			var pipeline = variant.pipelines.computeIfAbsent(key,
				unused -> this.prepare(variant, vanilla, override.vertexFormats(), phase, depthFormat));
			CapturedRenderingState.INSTANCE.setCurrentAlphaTest(this.alpha.reference());
			CapturedRenderingState.INSTANCE.setBlendMode(this.blend(vanilla));
			variant.uniforms.update(tick, frame);

			var bindings = new LinkedHashMap<String, List<GpuBufferSlice>>();

			int alignment = RenderSystem.getDevice().getDeviceInfo().limits().minUniformOffsetAlignment();

			for (var block : variant.layout.descriptors()) {
				if (block.set() == 0 && block.kind() == ShaderResourceLayout.Kind.UNIFORM_BUFFER) {
					GpuBufferSlice slice;

					if (block.name().equals(ShaderResourceLayout.UNIFORM_BLOCK)) {
						slice = RenderSystem.getDevice().createCommandEncoder().transientMemory()
							.uploadGpu(variant.uniforms.data(), alignment, GpuBuffer.USAGE_UNIFORM);
					} else if (block.name().equals("iris_SodiumPushConstants")) {
						if (pushConstants == null) {
							throw new IllegalStateException("missing Sodium push constants for " + this.name);
						}

						continue;
					} else {
						String keyName = block.name().startsWith("iris_") ? block.name().substring(5) : block.name();
						Object buffer = resources.get(keyName);

						if (!(buffer instanceof GpuBufferSlice)) {
							throw new IllegalStateException("missing world UBO for " + this.name + ": " + keyName);
						}

						slice = (GpuBufferSlice) buffer;
					}

					bindings.put(block.name(), List.of(slice));
				}
			}

			this.uniformBuffers = bindings;
			context.bindPipeline(pipeline);

			if (pushConstants != null) {
				context.pushConstants(pushConstants);
			}
		} finally {
			this.drawResources = Map.of();
			this.uniformBuffers = Map.of();
		}
	}

	public void prepare() {
		this.prepare(List.of(this.vertexFormat));
	}

	public void prepare(List<VertexFormat> formats) {
		if (this.closed) {
			throw new IllegalStateException("world program is closed: " + this.name);
		}

		var inputs = this.inputSources(formats);
		this.variants.forEach((phase, variant) -> this.prepareShader(variant, phase, inputs).attributes(formats));
	}

	private Map<PatchShaderType, String> inputSources(List<VertexFormat> formats) {
		return this.inputSources.computeIfAbsent(Collections.unmodifiableList(new ArrayList<>(formats)),
			key -> WorldShaderTransformer.vertexInputs(this.sources, WorldShaderTransformer.availableFormats(key)));
	}

	private PreparedShader prepareShader(Variant variant, WorldRenderTargets.Phase phase, Map<PatchShaderType, String> inputs) {
		return variant.shaders.computeIfAbsent(inputs.get(PatchShaderType.VERTEX), unused -> {
			var outputs = this.parent.getWorldTargets().getAttachmentPlan(phase, this.name).program(this.name);
			var patched = TransformPatcher.patchProgram(this.name, inputs, variant.samplers, variant.uniforms, outputs);
			var shader = Iris.getRenderBackend().createGraphicsShader(this.name, patched, variant.samplers, variant.uniforms);
			variant.uniforms.resolveLayout(shader.getSource().layout());
			variant.layout = shader.getSource().layout();

			return new PreparedShader(shader, new HashMap<>());
		});
	}

	private GraphicsPipeline prepare(Variant variant, RenderPipeline vanilla, List<VertexFormat> formats, WorldRenderTargets.Phase phase,
		GpuFormat depthFormat) {
		var prepared = this.prepareShader(variant, phase, this.inputSources(formats));
		var attributes = prepared.attributes(formats);
		int fallbackBinding = WorldShaderTransformer.fallbackVertexBinding(formats);
		boolean needsFallback = attributes.stream().anyMatch(attribute -> attribute.binding() == fallbackBinding);

		if (needsFallback && this.zeroVertices == null) {
			this.zeroVertices = RenderSystem.getDevice()
				.createBuffer(() -> "Iris zero vertex attributes", GpuBuffer.USAGE_VERTEX, ByteBuffer.allocateDirect(32));
		}

		var outputs = this.parent.getWorldTargets().getAttachmentPlan(phase, this.name).program(this.name);
		BlendMode blend = this.blend(vanilla);
		var blends = this.directives == null ? outputs.remapBlends(Collections.nCopies(this.drawBuffers.length, blend)) : outputs.blends(
			this.directives, blend);
		var targets = this.parent.getWorldTargets().getFramebuffer(phase, this.name).getColorViews();

		return prepared.shader().createPipeline(
			new PipelineState(new FramebufferDescription(targets.stream().map(v -> v.texture().getFormat()).toList(), depthFormat), blends,
				attributes, 0, vanilla, phase == WorldRenderTargets.Phase.SHADOW, needsFallback ? this.zeroVertices : null, formats));
	}

	private BlendMode blend(RenderPipeline vanilla) {
		BlendMode blend = null;

		if (this.blendOverride != null) {
			blend = this.blendOverride.blendMode();
		} else if (!vanilla.getColorTargetStates().isEmpty()) {
			var state = vanilla.getColorTargetStates().getFirst();

			if (state != null && state.blendFunction().isPresent()) {
				var function = state.blendFunction().get();
				blend = new BlendMode(GlConst.toGl(function.color().sourceFactor()), GlConst.toGl(function.color().destFactor()),
					GlConst.toGl(function.alpha().sourceFactor()), GlConst.toGl(function.alpha().destFactor()));
			}
		}

		return blend;
	}

	@Override
	public void close() {
		if (this.closed) {
			return;
		}

		this.closed = true;
		this.overrides.clear();

		if (this.zeroVertices != null) {
			this.zeroVertices.close();
		}

		var backend = Iris.getRenderBackend();

		for (var variant : this.variants.values()) {
			variant.pipelines.values().forEach(p -> backend.retire(p::close));
			variant.shaders.values().forEach(shader -> backend.retire(shader.shader()::close));
		}
	}

	public record OverrideKey(RenderPipeline source, FrontendRenderPipeline pipeline, List<VertexFormat> vertexFormats) {
	}

	public record PipelineKey(RenderPipeline pipeline, List<VertexFormat> vertexFormats, GpuFormat depthFormat) {
	}

	public record PreparedShader(GraphicsShader shader, Map<List<VertexFormat>, List<PipelineState.Attribute>> vertexAttributes) {
		private List<PipelineState.Attribute> attributes(List<VertexFormat> formats) {
			return this.vertexAttributes.computeIfAbsent(Collections.unmodifiableList(new ArrayList<>(formats)),
				key -> WorldShaderTransformer.attributes(this.shader.getSource().sources().get(PatchShaderType.VERTEX), key));
		}
	}

	public static class Variant {
		private final BufferUniformHolder uniforms;
		private final ProgramResources samplers;
		private final Map<PipelineKey, GraphicsPipeline> pipelines = new HashMap<>();
		private final Map<String, PreparedShader> shaders = new HashMap<>();
		private ShaderResourceLayout layout;

		public Variant(BufferUniformHolder uniforms, ProgramResources samplers, ShaderResourceLayout layout) {
			this.uniforms = uniforms;
			this.samplers = samplers;
			this.layout = layout;
		}
	}

}
