package net.irisshaders.iris.pipeline.programs;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.pipeline.IndexType;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import io.github.douira.glsl_transformer.ast.node.declaration.TypeAndInitDeclaration;
import io.github.douira.glsl_transformer.ast.node.type.qualifier.StorageQualifier.StorageType;
import io.github.douira.glsl_transformer.ast.node.type.specifier.BuiltinNumericTypeSpecifier;
import io.github.douira.glsl_transformer.ast.transform.JobParameters;
import io.github.douira.glsl_transformer.ast.transform.SingleASTTransformer;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.backend.FramebufferDescription;
import net.irisshaders.iris.backend.GraphicsPipeline;
import net.irisshaders.iris.backend.GraphicsShader;
import net.irisshaders.iris.backend.PipelineState;
import net.irisshaders.iris.backend.SyncCommand;
import net.irisshaders.iris.gl.blending.BlendMode;
import net.irisshaders.iris.gl.framebuffer.ViewportData;
import net.irisshaders.iris.pipeline.transform.PatchShaderType;
import net.irisshaders.iris.pipeline.transform.ShaderResourceLayout;
import net.irisshaders.iris.pipeline.transform.TransformPatcher;
import net.irisshaders.iris.pipeline.transform.WorldShaderTransformer;
import net.irisshaders.iris.pipeline.transform.vulkan.VulkanGlsl;
import net.irisshaders.iris.pipeline.transform.vulkan.VulkanShaderPatcher;
import net.irisshaders.iris.samplers.PackSamplers;
import net.irisshaders.iris.samplers.ProgramResources;
import net.irisshaders.iris.targets.ColorAttachmentPlan;
import net.irisshaders.iris.uniforms.buffer.BufferUniformHolder;
import org.joml.Vector2i;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class CompositeProgram {
	private static final int VERTEX_STRIDE = 56;
	private final String name;
	private final Map<PatchShaderType, String> sources;
	private final ColorAttachmentPlan.Program outputs;
	private final List<BlendMode> blends;
	private final BufferUniformHolder uniforms;
	private final ProgramResources samplers;
	private GraphicsPipeline pipeline;
	private GraphicsShader shader;

	private GpuBuffer vertices;
	private GpuBuffer quadIndices;
	private boolean destroyed;

	public CompositeProgram(PackSamplers pack, String name, Map<PatchShaderType, String> sources, ColorAttachmentPlan.Program outputs,
		List<BlendMode> blends) {
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
			if (resource.kind() != ShaderResourceLayout.Kind.STORAGE_BUFFER && resource.kind() != ShaderResourceLayout.Kind.SAMPLER && resource.kind() != ShaderResourceLayout.Kind.IMAGE && !resource.name()
				.equals(ShaderResourceLayout.UNIFORM_BLOCK)) {
				throw new UnsupportedOperationException("composite resource binding is not implemented: " + name + ": " + resource.name());
			}
		}

		this.uniforms = new BufferUniformHolder(layout);
		this.samplers = new ProgramResources(pack, layout);
	}

	private static List<PipelineState.Attribute> attributes(String source) {
		var tree = new SingleASTTransformer<JobParameters>().parseSeparateTranslationUnit(source);
		var attributes = new ArrayList<PipelineState.Attribute>();
		tree.getRoot().nodeIndex.getStream(TypeAndInitDeclaration.class).forEach(declaration -> {
			var qualifier = declaration.getType().getTypeQualifier();

			if (!VulkanGlsl.hasStorage(qualifier, StorageType.IN)) {
				return;
			}

			for (var member : declaration.getMembers()) {
				String name = member.getName().getName();

				int location = VulkanGlsl.integer(VulkanGlsl.layout(qualifier, "location").getExpression(), tree.getRoot());

				if (!name.equals("Position") && !name.equals("UV0")) {
					var type = VulkanGlsl.uniformType((BuiltinNumericTypeSpecifier) declaration.getType().getTypeSpecifier(), member,
						tree.getRoot());
					int columns = type.dimensions().stream().reduce(type.columns(), Math::multiplyExact);
					int locations = (type.rows() * type.scalarBytes() + 15) / 16;

					for (int column = 0; column < columns; column++) {
						attributes.add(
							new PipelineState.Attribute(location + column * locations, WorldShaderTransformer.zeroAttributeFormat(type),
								24));
					}

					continue;
				}

				attributes.add(new PipelineState.Attribute(location, name.equals("Position") ? GpuFormat.RGB32_FLOAT : GpuFormat.RG32_FLOAT,
					name.equals("Position") ? 0 : 12));
			}
		});

		return attributes;
	}

	public BufferUniformHolder getUniforms() {
		return this.uniforms;
	}

	public ProgramResources getSamplers() {
		return this.samplers;
	}

	public void prepare(List<GpuTextureView> targets) {
		if (this.destroyed) {
			throw new IllegalStateException("composite program is destroyed: " + this.name);
		}

		if (this.pipeline != null) {
			return;
		}

		var cleanup = new ArrayList<Runnable>();

		try {
			var patched = TransformPatcher.patchProgram(this.name, this.sources, this.samplers, this.uniforms, this.outputs);
			this.shader = Iris.getRenderBackend().createGraphicsShader(this.name, patched, this.samplers, this.uniforms);
			cleanup.add(this.shader::close);
			this.uniforms.resolveLayout(this.shader.getSource().layout());
			this.pipeline = this.shader.createPipeline(
				new PipelineState(new FramebufferDescription(targets.stream().map(v -> v.texture().getFormat()).toList(), null),
					this.blends, attributes(this.shader.getSource().sources().get(PatchShaderType.VERTEX)), VERTEX_STRIDE, null, false,
					null));

			cleanup.add(this.pipeline::close);

			try (var stack = MemoryStack.stackPush()) {
				var data = stack.calloc(4 * VERTEX_STRIDE).order(ByteOrder.nativeOrder());
				float[] corners = {0, 0, 1, 0, 1, 1, 0, 1};

				for (int vertex = 0; vertex < 4; vertex++) {
					int offset = vertex * VERTEX_STRIDE;
					data.putFloat(offset, corners[vertex * 2]);
					data.putFloat(offset + 4, corners[vertex * 2 + 1]);
					data.putFloat(offset + 12, corners[vertex * 2]);
					data.putFloat(offset + 16, corners[vertex * 2 + 1]);
				}

				this.vertices = RenderSystem.getDevice().createBuffer(() -> "Iris composite vertices", GpuBuffer.USAGE_VERTEX, data);
				cleanup.add(this.vertices::close);

				var indexData = stack.malloc(12).order(ByteOrder.nativeOrder());
				indexData.asShortBuffer().put(new short[]{0, 1, 2, 2, 3, 0});
				this.quadIndices = RenderSystem.getDevice().createBuffer(() -> "Iris composite indices", GpuBuffer.USAGE_INDEX, indexData);
			}
		} catch (RuntimeException | Error failure) {
			for (var close : cleanup.reversed()) {
				try {
					close.run();
				} catch (RuntimeException | Error suppressed) {
					failure.addSuppressed(suppressed);
				}
			}

			this.pipeline = null;
			this.shader = null;

			throw failure;
		}
	}

	public void draw(List<GpuTextureView> targets, ViewportData viewport, long tick, long frame) {
		this.prepare(targets);
		this.uniforms.update(tick, frame);
		var backend = Iris.getRenderBackend();
		var sync = backend.createSync();
		sync.read(this.pipeline);

		for (var target : targets) {
			sync.write(target, SyncCommand.Access.COLOR_ATTACHMENT);
		}

		sync.nextIs(SyncCommand.Stage.GRAPHICS).run();
		int width = targets.getFirst().getWidth(0);
		int height = targets.getFirst().getHeight(0);

		try (var pass = backend.createRenderPass(this.name, this.pipeline.framebuffer(), targets, null, new Vector2i(width, height))) {
			pass.setViewport((int) (viewport.viewportX() * width), (int) (viewport.viewportY() * height), (int) (width * viewport.scale()),
				(int) (height * viewport.scale()));
			pass.setVertexBuffer(0, this.vertices.slice());
			pass.bindPipeline(this.pipeline);

			if (this.quadIndices == null) {
				pass.draw(3, 1, 0, 0);
			} else {
				pass.setIndexBuffer(this.quadIndices, IndexType.SHORT);
				pass.drawIndexed(6, 1, 0, 0, 0);
			}
		}
	}

	public void destroy() {
		if (this.destroyed) {
			return;
		}

		this.destroyed = true;

		if (this.vertices != null) {
			this.vertices.close();
		}

		if (this.quadIndices != null) {
			this.quadIndices.close();
		}

		if (this.pipeline != null) {
			Iris.getRenderBackend().retire(this.pipeline::close);
		}

		if (this.shader != null) {
			Iris.getRenderBackend().retire(this.shader::close);
		}
	}

}
