package net.irisshaders.iris.vulkan;

import com.mojang.renderpearl.api.textures.GpuTexture;
import com.mojang.renderpearl.backend.vulkan.VulkanCommandEncoder;
import com.mojang.renderpearl.backend.vulkan.VulkanGpuTexture;
import net.irisshaders.iris.Iris;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.KHRSynchronization2;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkDependencyInfo;
import org.lwjgl.vulkan.VkMemoryBarrier2;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VulkanSynchronization {
	private static final Dependency WORLD_PASS_DEPENDENCY = new Dependency(KHRSynchronization2.VK_PIPELINE_STAGE_2_ALL_GRAPHICS_BIT_KHR,
		KHRSynchronization2.VK_ACCESS_2_MEMORY_READ_BIT_KHR | KHRSynchronization2.VK_ACCESS_2_MEMORY_WRITE_BIT_KHR,
		KHRSynchronization2.VK_PIPELINE_STAGE_2_ALL_GRAPHICS_BIT_KHR | KHRSynchronization2.VK_PIPELINE_STAGE_2_COMPUTE_SHADER_BIT_KHR | KHRSynchronization2.VK_PIPELINE_STAGE_2_ALL_TRANSFER_BIT_KHR,
		KHRSynchronization2.VK_ACCESS_2_MEMORY_READ_BIT_KHR | KHRSynchronization2.VK_ACCESS_2_MEMORY_WRITE_BIT_KHR);
	private final Planner planner = new Planner();
	private boolean started;

	public static Resource image(GpuTexture texture) {
		return new Resource(true, ((VulkanGpuTexture) texture).vkImage());
	}

	public static Access shaderImage(long image, long stage, boolean write) {
		return new Access(new Resource(true, image), stage,
			KHRSynchronization2.VK_ACCESS_2_SHADER_READ_BIT_KHR | (write ? KHRSynchronization2.VK_ACCESS_2_SHADER_WRITE_BIT_KHR : 0),
			write);
	}

	public static void before(VkCommandBuffer command, MemoryStack stack, List<Access> accesses) {
		var sync = ((VulkanRenderBackend) Iris.getRenderBackend()).synchronization;

		if (!sync.started) {
			VulkanCommandEncoder.memoryBarrier(command, stack);
			sync.started = true;
		}

		var dependency = sync.planner.accept(accesses);

		if (dependency.srcStages() != 0) {
			barrier(command, stack, dependency);
		}
	}

	public static void afterWorldPass(VkCommandBuffer command, MemoryStack stack) {
		barrier(command, stack, WORLD_PASS_DEPENDENCY);
		reset();
	}

	private static void barrier(VkCommandBuffer command, MemoryStack stack, Dependency dependency) {
		var memory = VkMemoryBarrier2.calloc(1, stack).sType$Default().srcStageMask(dependency.srcStages())
			.srcAccessMask(dependency.srcAccess()).dstStageMask(dependency.dstStages()).dstAccessMask(dependency.dstAccess());
		KHRSynchronization2.vkCmdPipelineBarrier2KHR(command, VkDependencyInfo.calloc(stack).sType$Default().pMemoryBarriers(memory));
	}

	public static void after(VkCommandBuffer command, MemoryStack stack) {
		VulkanCommandEncoder.memoryBarrier(command, stack);
		reset();
	}

	public static void mip(VkCommandBuffer command, MemoryStack stack, GpuTexture texture) {
		before(command, stack, List.of(new Access(image(texture), KHRSynchronization2.VK_PIPELINE_STAGE_2_TRANSFER_BIT_KHR,
			KHRSynchronization2.VK_ACCESS_2_TRANSFER_READ_BIT_KHR | KHRSynchronization2.VK_ACCESS_2_TRANSFER_WRITE_BIT_KHR, true)));
	}

	private static void reset() {
		var sync = ((VulkanRenderBackend) Iris.getRenderBackend()).synchronization;
		sync.planner.states.clear();
		sync.started = false;
	}

	public record Resource(boolean image, long handle) {
	}

	public record Access(Resource resource, long stages, long access, boolean write) {
	}

	public record Dependency(long srcStages, long srcAccess, long dstStages, long dstAccess) {
	}

	public static class Planner {
		private final Map<Resource, State> states = new HashMap<>();

		public Dependency accept(List<Access> accesses) {
			var merged = new HashMap<Resource, Access>();

			for (var access : accesses) {
				merged.merge(access.resource(), access,
					(a, b) -> new Access(a.resource(), a.stages() | b.stages(), a.access() | b.access(), a.write() || b.write()));
			}

			long srcStages = 0;
			long srcAccess = 0;
			long dstStages = 0;
			long dstAccess = 0;

			for (var access : merged.values()) {
				for (var entry : this.states.entrySet()) {
					var resource = entry.getKey();

					if (resource.image() != access.resource().image() || resource.handle() != 0 && access.resource()
						.handle() != 0 && !resource.equals(access.resource())) {
						continue;
					}

					var state = entry.getValue();

					if (access.write()) {
						srcStages |= state.writerStages | state.readerStages;
						srcAccess |= state.writerAccess;
						dstStages |= access.stages();
						dstAccess |= access.access();
					} else if ((state.visibleStages & access.stages()) != access.stages() || (state.visibleAccess & access.access()) != access.access()) {
						srcStages |= state.writerStages;
						srcAccess |= state.writerAccess;
						dstStages |= access.stages();
						dstAccess |= access.access();
					}
				}
			}

			for (var access : merged.values()) {
				var state = this.states.computeIfAbsent(access.resource(), ignored -> new State());

				if (access.write()) {
					state.writerStages = access.stages();
					state.writerAccess = access.access();
					state.readerStages = state.visibleStages = state.visibleAccess = 0;
				} else {
					state.readerStages |= access.stages();
					state.visibleStages |= access.stages();
					state.visibleAccess |= access.access();
				}
			}

			return new Dependency(srcStages, srcAccess, dstStages, dstAccess);
		}

		public static class State {
			private long writerStages;
			private long writerAccess;
			private long readerStages;
			private long visibleStages;
			private long visibleAccess;
		}
	}
}
