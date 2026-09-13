package net.irisshaders.iris.pipeline.programs;

import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.device.GpuDevice;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import com.mojang.renderpearl.backend.vulkan.VulkanConst;
import io.github.douira.glsl_transformer.ast.node.declaration.TypeAndInitDeclaration;
import io.github.douira.glsl_transformer.ast.node.type.qualifier.StorageQualifier.StorageType;
import io.github.douira.glsl_transformer.ast.transform.JobParameters;
import io.github.douira.glsl_transformer.ast.transform.SingleASTTransformer;
import net.irisshaders.iris.gl.blending.BlendMode;
import net.irisshaders.iris.gl.framebuffer.ViewportData;
import net.irisshaders.iris.pipeline.transform.PatchShaderType;
import net.irisshaders.iris.pipeline.transform.vulkan.VulkanGlsl;
import net.irisshaders.iris.pipeline.transform.vulkan.VulkanResourceLayout;
import net.irisshaders.iris.pipeline.transform.vulkan.VulkanShaderPatcher;
import net.irisshaders.iris.samplers.PackSamplers;
import net.irisshaders.iris.samplers.ProgramResources;
import net.irisshaders.iris.samplers.SamplerBindingPlan;
import net.irisshaders.iris.targets.ColorAttachmentPlan;
import net.irisshaders.iris.uniforms.buffer.BufferUniformHolder;
import net.irisshaders.iris.vulkan.CompositeSynchronization;
import net.irisshaders.iris.vulkan.VulkanCommandBridge;
import net.irisshaders.iris.vulkan.VulkanGraphicsPipeline;
import net.irisshaders.iris.vulkan.VulkanResourceSets;
import net.irisshaders.iris.vulkan.VulkanShaderCompiler;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.VK10;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class CompositeProgram {
	private final GpuDevice device;
	private final PackSamplers pack;
	private final String name;
	private final Map<PatchShaderType, String> sources;
	private final ColorAttachmentPlan.Program outputs;
	private final List<BlendMode> blends;
	private final BufferUniformHolder uniforms;
	private final ProgramResources samplers;
	private VulkanGraphicsPipeline pipeline;
	private VulkanResourceSets uniformSets;
	private GpuBuffer vertices;
	private GpuBuffer quadIndices;
	private boolean destroyed;

	public CompositeProgram(GpuDevice device, PackSamplers pack, String name, Map<PatchShaderType, String> sources,
		ColorAttachmentPlan.Program outputs, List<BlendMode> blends) {
		this.device = device;
		this.pack = pack;
		this.name = name;

		var filtered = new EnumMap<PatchShaderType, String>(PatchShaderType.class);
		sources.forEach((stage, text) -> {
			if (text != null) {
				filtered.put(stage, text);
			}
		});
		this.sources = Map.copyOf(filtered);
		this.outputs = outputs;
		this.blends = blends;

		var layout = VulkanShaderPatcher.discover(sources).layout();

		for (var resource : layout.descriptors()) {
			if (resource.kind() != VulkanResourceLayout.Kind.STORAGE_BUFFER && resource.kind() != VulkanResourceLayout.Kind.SAMPLER &&
				resource.kind() != VulkanResourceLayout.Kind.IMAGE && !resource.name().equals(VulkanResourceLayout.UNIFORM_BLOCK)) {
				throw new UnsupportedOperationException("Composite resource binding is not implemented: " + name + ": " + resource.name());
			}
		}

		uniforms = new BufferUniformHolder(layout);
		samplers = new ProgramResources(pack, layout);
	}

	public BufferUniformHolder uniforms() {
		return uniforms;
	}

	public ProgramResources samplers() {
		return samplers;
	}

	public void prepare(List<GpuTextureView> targets) {
		if (destroyed) {
			throw new IllegalStateException("Composite program is destroyed: " + name);
		}

		if (pipeline != null) {
			return;
		}

		var shader = VulkanShaderPatcher.patch(sources, samplers.plan(), outputs, uniforms::isRegistered, uniforms.frameLayout(),
			uniforms::isDynamic);
		uniforms.resolveLayout(shader.layout());

		var backend = pack.backend();
		var cleanup = new ArrayList<Runnable>();

		try {
			if (shader.layout().descriptors().stream().anyMatch(d -> d.set() == 0)) {
				uniformSets = new VulkanResourceSets(backend.vkDevice(), shader.layout(), 16);
				cleanup.add(uniformSets::close);
			}

			var stages = new EnumMap<PatchShaderType, ByteBuffer>(PatchShaderType.class);

			try (var compiler = new VulkanShaderCompiler()) {
				for (var entry : shader.sources().entrySet()) {
					stages.put(entry.getKey(), compiler.compile(name + entry.getKey().extension, entry.getKey(), entry.getValue()));
				}

				pipeline = new VulkanGraphicsPipeline(backend.vkDevice(), pack.heap(), uniformSets, stages,
					targets.stream().mapToInt(v -> VulkanConst.toVk(v.texture().getFormat())).toArray(),
					20, attributes(shader.sources().get(PatchShaderType.VERTEX)), blends,
					shader.writtenAttachments(), null,
					uniforms.frameUniforms() == null ? null : uniforms.frameUniforms().sets());
			} finally {
				stages.values().forEach(buffer -> MemoryUtil.memFree(buffer));
			}

			cleanup.add(pipeline::close);

			try (var stack = MemoryStack.stackPush()) {
				var data = stack.malloc(80).order(ByteOrder.nativeOrder());
				data.asFloatBuffer().put(new float[]{0, 0, 0, 0, 0, 1, 0, 0, 1, 0, 1, 1, 0, 1, 1, 0, 1, 0, 0, 1});
				vertices = device.createBuffer(() -> "Iris composite vertices", GpuBuffer.USAGE_VERTEX, data);
				cleanup.add(vertices::close);

				var indexData = stack.malloc(12).order(ByteOrder.nativeOrder());
				indexData.asShortBuffer().put(new short[]{0, 1, 2, 2, 3, 0});
				quadIndices = device.createBuffer(() -> "Iris composite indices", GpuBuffer.USAGE_INDEX, indexData);
			}
		} catch (RuntimeException | Error failure) {
			for (var close : cleanup.reversed()) {
				try {
					close.run();
				} catch (RuntimeException | Error suppressed) {
					failure.addSuppressed(suppressed);
				}
			}

			pipeline = null;
			uniformSets = null;

			throw failure;
		}
	}

	private static List<VulkanGraphicsPipeline.Attribute> attributes(String source) {
		var tree = new SingleASTTransformer<JobParameters>().parseSeparateTranslationUnit(source);
		var attributes = new ArrayList<VulkanGraphicsPipeline.Attribute>();
		tree.getRoot().nodeIndex.getStream(TypeAndInitDeclaration.class).forEach(declaration -> {
			var qualifier = declaration.getType().getTypeQualifier();

			if (!VulkanGlsl.hasStorage(qualifier, StorageType.IN)) {
				return;
			}

			for (var member : declaration.getMembers()) {
				String name = member.getName().getName();

				if (!name.equals("Position") && !name.equals("UV0")) {
					throw new IllegalArgumentException("Unknown composite vertex input: " + name);
				}

				int location = VulkanGlsl.integer(VulkanGlsl.layout(qualifier, "location").getExpression(), tree.getRoot());
				attributes.add(new VulkanGraphicsPipeline.Attribute(
					location, name.equals("Position") ? VK10.VK_FORMAT_R32G32B32_SFLOAT : VK10.VK_FORMAT_R32G32_SFLOAT,
					name.equals("Position") ? 0 : 12));
			}
		});

		return attributes;
	}

	public void draw(List<GpuTextureView> targets, ViewportData viewport, long tick, long frame) {
		prepare(targets);
		uniforms.update(tick, frame);

		var encoder = device.createCommandEncoder();
		var snapshot = samplers.snapshot();

		try {
			var blocks = new LinkedHashMap<>(samplers.bufferBindings());
			var indices = samplers.uploadIndices(VulkanCommandBridge.encoder(encoder));

			if (indices != null) {
				blocks.put(SamplerBindingPlan.INDEX_BLOCK, List.of(indices));
			}

			if (CompositeSynchronization.active()) {
				CompositeSynchronization.resources(samplers.accesses());
			}

			VulkanCommandBridge.drawBatch(encoder, device.getDeviceInfo().limits().minUniformOffsetAlignment(),
				List.of(new VulkanCommandBridge.Draw(pipeline, uniforms, uniformSets, blocks, snapshot, vertices,
					viewport, samplers.imageViews(), quadIndices)),
				targets);
		} finally {
			VulkanCommandBridge.retire(encoder, snapshot::close);
		}
	}

	public void destroy() {
		if (destroyed) {
			return;
		}

		destroyed = true;

		if (pipeline != null) {
			var encoder = device.createCommandEncoder();
			VulkanCommandBridge.retire(encoder, pipeline::close);

			if (uniformSets != null) {
				VulkanCommandBridge.retire(encoder, uniformSets::close);
			}

			vertices.close();
			quadIndices.close();
		}
	}
}
