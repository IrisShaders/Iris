package net.irisshaders.iris.uniforms.buffer;

import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.device.GpuDevice;
import com.mojang.renderpearl.backend.vulkan.VulkanDevice;
import kroppeb.stareval.function.Type;
import net.irisshaders.iris.gl.uniform.UniformType;
import net.irisshaders.iris.pipeline.transform.PatchShaderType;
import net.irisshaders.iris.pipeline.transform.vulkan.Std140Type;
import net.irisshaders.iris.pipeline.transform.vulkan.VulkanResourceLayout;
import net.irisshaders.iris.uniforms.custom.CustomUniforms;
import net.irisshaders.iris.vulkan.VulkanCommandBridge;
import net.irisshaders.iris.vulkan.VulkanResourceSets;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public final class FrameUniforms implements AutoCloseable {
	private final GpuDevice device;
	private final VulkanDevice backend;
	private final VulkanResourceLayout layout;
	private final BufferUniformHolder values;
	private VulkanResourceSets sets;
	private Slot current;
	private final ArrayDeque<Slot> available = new ArrayDeque<>();
	private final List<Slot> slots = new ArrayList<>();
	private boolean closed;
	private int uploads;

	public FrameUniforms(GpuDevice device, VulkanDevice backend, CustomUniforms custom) {
		this.device = device;
		this.backend = backend;

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

		var uniforms = new LinkedHashMap<String, VulkanResourceLayout.Uniform>();
		int offset = 0;

		for (var entry : types.entrySet()) {
			offset = Std140Type.align(offset, entry.getValue().alignment());
			uniforms.put(entry.getKey(), new VulkanResourceLayout.Uniform(offset, entry.getValue()));
			offset += entry.getValue().size();
		}

		layout = new VulkanResourceLayout(
			Std140Type.align(offset, 16), uniforms,
			List.of(new VulkanResourceLayout.Descriptor(VulkanResourceLayout.FRAME_BLOCK, VulkanResourceLayout.Kind.UNIFORM_BUFFER,
				"block",
				1, 2, 0, null, EnumSet.allOf(PatchShaderType.class))));
		values = new BufferUniformHolder(layout);
		custom.assignTo(values);
	}

	public VulkanResourceLayout layout() {
		return layout;
	}

	public int uploads() {
		return uploads;
	}

	public VulkanResourceSets sets() {
		if (closed) {
			throw new IllegalStateException("Frame uniforms are closed");
		}

		if (sets == null) {
			sets = new VulkanResourceSets(backend.vkDevice(), layout, 64, 2);
		}

		return sets;
	}

	public void beginFrame() {
		retireFrame();
		snapshot();
	}

	public VulkanResourceSets.Snapshot snapshot() {
		if (closed) {
			throw new IllegalStateException("Frame uniforms are closed");
		}

		if (current == null) {
			current = available.pollFirst();

			if (current == null) {
				var buffer = device.createBuffer(
					() -> "Iris frame uniforms", GpuBuffer.USAGE_UNIFORM | GpuBuffer.USAGE_COPY_DST, layout.uniformBufferSize());

				try {
					var descriptor =
						sets().snapshot(Map.of(VulkanResourceLayout.FRAME_BLOCK,
							List.of(VulkanCommandBridge.bufferSlice(buffer.slice()))));
					current = new Slot(buffer, descriptor);
					slots.add(current);
				} catch (RuntimeException | Error failure) {
					buffer.close();

					throw failure;
				}
			}

			values.update(0, uploads);
			device.createCommandEncoder().writeToBuffer(current.buffer().slice(), values.data());
			uploads++;
		}

		return current.descriptor();
	}

	private void retireFrame() {
		if (current != null) {
			var completed = current;
			VulkanCommandBridge.retire(device.createCommandEncoder(), () -> {
				if (!closed) {
					available.addLast(completed);
				}
			});
			current = null;
		}
	}

	@Override
	public void close() {
		if (closed) {
			return;
		}

		closed = true;

		var encoder = device.createCommandEncoder();

		for (var slot : slots) {
			VulkanCommandBridge.retire(encoder, slot.descriptor()::close);
			slot.buffer().close();
		}

		if (sets != null) {
			VulkanCommandBridge.retire(encoder, sets::close);
		}

		current = null;
		available.clear();
		slots.clear();
	}

	private record Slot(GpuBuffer buffer, VulkanResourceSets.Snapshot descriptor) {
	}
}
