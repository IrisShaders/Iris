package net.irisshaders.iris.mixin;

import com.mojang.renderpearl.backend.vulkan.VulkanCommandEncoder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.lwjgl.vulkan.VkCommandBuffer;

@Mixin(VulkanCommandEncoder.class)
public interface VulkanCommandEncoderAccessor {
	@Invoker("objectInitCommandBuffer")
	public VkCommandBuffer iris$objectInitCommandBuffer();

	@Accessor("currentSubmitIndex")
	public long iris$getCurrentSubmitIndex();

	@Accessor("completedSubmitIndex")
	public long iris$getCompletedSubmitIndex();
}
