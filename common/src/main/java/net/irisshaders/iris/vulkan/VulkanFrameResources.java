package net.irisshaders.iris.vulkan;

import com.mojang.renderpearl.api.buffers.GpuBuffer;
import net.irisshaders.iris.pipeline.transform.ShaderResourceLayout;
import net.irisshaders.iris.uniforms.buffer.FrameUniforms;
import org.lwjgl.vulkan.VkDevice;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public class VulkanFrameResources implements AutoCloseable {
	private static final Map<FrameUniforms, VulkanFrameResources> FRAMES = new IdentityHashMap<>();
	public final VulkanResourceSets sets;
	private final FrameUniforms uniforms;
	private final Map<GpuBuffer, VulkanResourceSets.Snapshot> snapshots = new IdentityHashMap<>();

	private VulkanFrameResources(FrameUniforms uniforms, VkDevice device) {
		this.uniforms = uniforms;
		this.sets = new VulkanResourceSets(device, uniforms.getLayout(), 64, 2);
	}

	public static VulkanFrameResources getOrCreate(FrameUniforms uniforms, VkDevice device) {
		if (uniforms == null) {
			return null;
		}

		return FRAMES.computeIfAbsent(uniforms, key -> {
			var result = new VulkanFrameResources(key, device);
			key.addCloseCallback(() -> {
				FRAMES.remove(key);
				result.close();
			});

			return result;
		});
	}

	public VulkanResourceSets.Snapshot getSnapshot() {
		var slice = this.uniforms.snapshot();

		return this.snapshots.computeIfAbsent(slice.buffer(),
			key -> this.sets.snapshot(Map.of(ShaderResourceLayout.FRAME_BLOCK, List.of(VulkanCommandBridge.bufferSlice(slice)))));
	}

	@Override
	public void close() {
		this.snapshots.values().forEach(VulkanResourceSets.Snapshot::close);
		this.snapshots.clear();
		this.sets.close();
	}
}
