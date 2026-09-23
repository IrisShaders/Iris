package net.irisshaders.iris.uniforms.buffer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import kroppeb.stareval.function.Type;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.gl.uniform.UniformType;
import net.irisshaders.iris.pipeline.transform.PatchShaderType;
import net.irisshaders.iris.pipeline.transform.ShaderResourceLayout;
import net.irisshaders.iris.pipeline.transform.Std140Type;
import net.irisshaders.iris.uniforms.custom.CustomUniforms;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.TreeMap;

public final class FrameUniforms implements AutoCloseable {
	private final ShaderResourceLayout layout;
	private final BufferUniformHolder values;
	private final ArrayDeque<Slot> available = new ArrayDeque<>();
	private final List<Slot> slots = new ArrayList<>();
	private final List<Runnable> cleanup = new ArrayList<>();
	private Slot current;
	private boolean closed;
	private int uploads;

	public FrameUniforms(CustomUniforms custom) {
		var types = new TreeMap<String, Std140Type>();

		for (var uniform : custom.frameUniforms()) {
			UniformType type = Type.convert(uniform.getType());
			String glsl = switch (type) {
				case INT -> uniform.getType().equals(Type.Boolean) ? "bool" : "int";
				case FLOAT -> "float";
				case VEC2 -> "vec2";
				case VEC3 -> "vec3";
				case VEC4 -> "vec4";
				case VEC2I -> "ivec2";
				case VEC3I -> "ivec3";
				case VEC4I -> "ivec4";
				case MAT3 -> "mat3";
				case MAT4 -> "mat4";
			};
			int rows = switch (type) {
				case VEC2, VEC2I -> 2;
				case VEC3, VEC3I, MAT3 -> 3;
				case VEC4, VEC4I, MAT4 -> 4;
				default -> 1;
			};
			types.put(uniform.getName(),
				new Std140Type(glsl, 4, type == UniformType.MAT3 || type == UniformType.MAT4 ? rows : 1, rows, List.of()));
		}

		var uniforms = new LinkedHashMap<String, ShaderResourceLayout.Uniform>();
		int offset = 0;

		for (var entry : types.entrySet()) {
			offset = Std140Type.align(offset, entry.getValue().alignment());
			uniforms.put(entry.getKey(), new ShaderResourceLayout.Uniform(offset, entry.getValue()));
			offset += entry.getValue().size();
		}

		this.layout = new ShaderResourceLayout(Std140Type.align(offset, 16), uniforms, List.of(
			new ShaderResourceLayout.Descriptor(ShaderResourceLayout.FRAME_BLOCK, ShaderResourceLayout.Kind.UNIFORM_BUFFER, "block", 1, 2,
				0, null, EnumSet.allOf(PatchShaderType.class))));
		this.values = new BufferUniformHolder(this.layout);
		custom.assignTo(this.values);
	}

	public void addCloseCallback(Runnable destructor) {
		this.cleanup.add(destructor);
	}

	public ShaderResourceLayout getLayout() {
		return this.layout;
	}

	public void beginFrame() {
		this.retireFrame();
		this.snapshot();
	}

	public GpuBufferSlice snapshot() {
		if (this.closed) {
			throw new IllegalStateException("frame uniforms are closed");
		}

		if (this.current == null) {
			this.current = this.available.pollFirst();

			if (this.current == null) {
				var buffer = RenderSystem.getDevice()
					.createBuffer(() -> "Iris frame uniforms", GpuBuffer.USAGE_UNIFORM | GpuBuffer.USAGE_COPY_DST,
						this.layout.uniformBufferSize());

				this.current = new Slot(buffer, buffer.slice());
				this.slots.add(this.current);
			}

			this.values.update(0, this.uploads);
			RenderSystem.getDevice().createCommandEncoder().writeToBuffer(this.current.buffer().slice(), this.values.data());
			this.uploads++;
		}

		return this.current.slice();
	}

	private void retireFrame() {
		if (this.current != null) {
			var completed = this.current;
			Iris.getRenderBackend().retire(() -> {
				if (!this.closed) {
					this.available.addLast(completed);
				}
			});
			this.current = null;
		}
	}

	@Override
	public void close() {
		if (this.closed) {
			return;
		}

		this.closed = true;
		this.cleanup.forEach(Iris.getRenderBackend()::retire);
		this.cleanup.clear();

		for (var slot : this.slots) {

			slot.buffer().close();
		}

		this.current = null;
		this.available.clear();
		this.slots.clear();
	}

	private record Slot(GpuBuffer buffer, GpuBufferSlice slice) {
	}
}
