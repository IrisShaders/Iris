package net.irisshaders.iris.vulkan;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkDependencyInfo;
import org.lwjgl.vulkan.VkMemoryBarrier2;

import static org.lwjgl.vulkan.KHRSynchronization2.*;

public class VulkanSynchronization {
	public static final long GRAPHICS = VK_PIPELINE_STAGE_2_ALL_GRAPHICS_BIT_KHR | VK_PIPELINE_STAGE_2_DRAW_INDIRECT_BIT_KHR;
	public static final long GRAPHICS_READS = VK_ACCESS_2_SHADER_READ_BIT_KHR | VK_ACCESS_2_UNIFORM_READ_BIT_KHR
		| VK_ACCESS_2_VERTEX_ATTRIBUTE_READ_BIT_KHR | VK_ACCESS_2_INDEX_READ_BIT_KHR | VK_ACCESS_2_INDIRECT_COMMAND_READ_BIT_KHR
		| VK_ACCESS_2_COLOR_ATTACHMENT_READ_BIT_KHR | VK_ACCESS_2_DEPTH_STENCIL_ATTACHMENT_READ_BIT_KHR;
	public static final long GRAPHICS_WRITES = VK_ACCESS_2_SHADER_WRITE_BIT_KHR | VK_ACCESS_2_COLOR_ATTACHMENT_WRITE_BIT_KHR
		| VK_ACCESS_2_DEPTH_STENCIL_ATTACHMENT_WRITE_BIT_KHR;

	private long writerStages;
	private long writerAccess;
	private long readerStages;

	public void sync(long stage, long reads, long writes) {
		if ((reads | writes) == 0) return;
		if (stage == 0) throw new IllegalArgumentException("memory accesses require a pipeline stage");
		long source = this.writerStages | (writes == 0 ? 0 : this.readerStages);
		if (source != 0) {
			try (var stack = MemoryStack.stackPush()) {
				var memory = VkMemoryBarrier2.calloc(1, stack).sType$Default().srcStageMask(source).srcAccessMask(this.writerAccess)
					.dstStageMask(stage).dstAccessMask(reads | writes);
				vkCmdPipelineBarrier2KHR(VulkanCommandBridge.commandBuffer(), VkDependencyInfo.calloc(stack).sType$Default().pMemoryBarriers(memory));
			}
		}
		if (writes != 0) {
			// A write to one category must not erase producers in other categories.
			this.writerStages |= stage;
			this.writerAccess |= writes;
		}
		if (reads != 0) this.readerStages |= stage;
	}
}
