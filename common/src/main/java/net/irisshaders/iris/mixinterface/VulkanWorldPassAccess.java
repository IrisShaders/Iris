package net.irisshaders.iris.mixinterface;

import com.mojang.renderpearl.api.commands.RenderPassDescriptor;
import com.mojang.renderpearl.backend.api.RenderPassBackend;
import net.irisshaders.iris.vulkan.VulkanWorldCommands;

public interface VulkanWorldPassAccess {
	RenderPassBackend iris$createRenderPass(RenderPassDescriptor descriptor);

	void iris$setWorldPass(VulkanWorldCommands pass);
}
