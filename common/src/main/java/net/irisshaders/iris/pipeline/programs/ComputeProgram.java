package net.irisshaders.iris.pipeline.programs;

import net.irisshaders.iris.Iris;
import net.irisshaders.iris.backend.ComputePipeline;
import net.irisshaders.iris.pipeline.transform.PatchShaderType;
import net.irisshaders.iris.pipeline.transform.ShaderResourceLayout;
import net.irisshaders.iris.pipeline.transform.TransformPatcher;
import net.irisshaders.iris.pipeline.transform.vulkan.VulkanShaderPatcher;
import net.irisshaders.iris.samplers.PackSamplers;
import net.irisshaders.iris.samplers.ProgramResources;
import net.irisshaders.iris.shaderpack.FilledIndirectPointer;
import net.irisshaders.iris.uniforms.SystemTimeUniforms;
import net.irisshaders.iris.uniforms.buffer.BufferUniformHolder;
import net.minecraft.client.Minecraft;
import org.joml.Vector2f;
import org.joml.Vector3i;

import java.util.Map;

public final class ComputeProgram implements AutoCloseable {
	private final String name;
	private final String source;
	private final BufferUniformHolder uniforms;
	private final ProgramResources samplers;
	private FilledIndirectPointer indirect;
	private Vector3i absoluteWorkGroups;
	private Vector2f relativeWorkGroups;
	private float cachedWidth;
	private float cachedHeight;
	private Vector3i cachedWorkGroups;
	private ComputePipeline pipeline;
	private boolean closed;

	public ComputeProgram(PackSamplers pack, String name, String source) {
		this.name = name;
		this.source = source;

		var resources = VulkanShaderPatcher.discover(Map.of(PatchShaderType.COMPUTE, source)).layout();

		for (var resource : resources.descriptors()) {
			if (resource.kind() != ShaderResourceLayout.Kind.STORAGE_BUFFER && resource.kind() != ShaderResourceLayout.Kind.SAMPLER && resource.kind() != ShaderResourceLayout.Kind.IMAGE && !resource.name()
				.equals(ShaderResourceLayout.UNIFORM_BLOCK)) {
				throw new UnsupportedOperationException("unsupported compute resource: " + name + ": " + resource.name());
			}
		}

		this.uniforms = new BufferUniformHolder(resources);
		this.samplers = new ProgramResources(pack, resources);
	}

	public void setWorkGroupInfo(Vector2f relativeWorkGroups, Vector3i absoluteWorkGroups, FilledIndirectPointer indirectPointer) {
		this.relativeWorkGroups = relativeWorkGroups;
		this.absoluteWorkGroups = absoluteWorkGroups;
		this.indirect = indirectPointer;
		this.cachedWorkGroups = null;
	}

	public Vector3i getWorkGroups(float width, float height) {
		var localSize = this.localSize();

		if (this.cachedWidth != width || this.cachedHeight != height || this.cachedWorkGroups == null) {
			this.cachedWidth = width;
			this.cachedHeight = height;

			if (this.absoluteWorkGroups != null) {
				this.cachedWorkGroups = this.absoluteWorkGroups;
			} else if (this.relativeWorkGroups != null) {
				this.cachedWorkGroups = new Vector3i((int) Math.ceil(Math.ceil((width * this.relativeWorkGroups.x)) / localSize.x),
					(int) Math.ceil(Math.ceil((height * this.relativeWorkGroups.y)) / localSize.y), 1);
			} else {
				this.cachedWorkGroups = new Vector3i((int) Math.ceil(width / localSize.x), (int) Math.ceil(height / localSize.y), 1);
			}
		}

		return this.cachedWorkGroups;
	}

	public void dispatch(float width, float height) {
		var level = Minecraft.getInstance().level;
		this.dispatch(this.indirect == null ? this.getWorkGroups(width, height) : new Vector3i(), level == null ? 0 : level.getGameTime(),
			SystemTimeUniforms.COUNTER.getAsInt());
	}

	public void destroy() {
		this.close();
	}

	public BufferUniformHolder getUniforms() {
		return this.uniforms;
	}

	public ProgramResources getSamplers() {
		return this.samplers;
	}

	public Vector3i localSize() {
		this.prepare();

		return this.pipeline.localSize();
	}

	public void prepare() {
		if (this.closed) {
			throw new IllegalStateException("compute program is closed: " + this.name);
		}

		if (this.pipeline == null) {
			var patched = TransformPatcher.patchProgram(this.name, Map.of(PatchShaderType.COMPUTE, this.source), this.samplers,
				this.uniforms, null);
			this.pipeline = Iris.getRenderBackend().createComputePipeline(this.name, patched, this.samplers, this.uniforms);
		}
	}

	public void dispatch(Vector3i groups, long tick, long frame) {
		this.prepare();
		this.uniforms.update(tick, frame);
		Iris.getRenderBackend().commands().dispatch(this.pipeline, groups, this.indirect);
	}

	@Override
	public void close() {
		if (this.closed) {
			return;
		}

		this.closed = true;

		if (this.pipeline != null) {
			Iris.getRenderBackend().retire(this.pipeline::close);
		}
	}
}
