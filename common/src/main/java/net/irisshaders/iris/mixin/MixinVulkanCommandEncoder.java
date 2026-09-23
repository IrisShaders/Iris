package net.irisshaders.iris.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.renderpearl.api.commands.RenderPassDescriptor;
import com.mojang.renderpearl.backend.api.RenderPassBackend;
import com.mojang.renderpearl.backend.vulkan.VulkanCommandEncoder;
import com.mojang.renderpearl.backend.vulkan.VulkanRenderPass;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.mixinterface.VulkanWorldPassAccess;
import net.irisshaders.iris.vulkan.VulkanRenderBackend;
import net.irisshaders.iris.vulkan.VulkanWorldCommands;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkRenderingInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(VulkanCommandEncoder.class)
public abstract class MixinVulkanCommandEncoder implements VulkanWorldPassAccess {
	@Shadow
	public VulkanRenderPass currentRenderPass;
	@Unique
	private VulkanWorldCommands iris$worldPass;
	@Unique
	private VulkanWorldCommands iris$closingWorldPass;

	@Unique
	private RenderPassDescriptor iris$creatingPass;

	@Override
	public RenderPassBackend iris$createRenderPass(RenderPassDescriptor descriptor) {
		if (this.currentRenderPass != null || this.iris$creatingPass != null) {
			throw new IllegalStateException("a render pass is already open");
		}

		this.iris$creatingPass = descriptor;
		try {
			return ((VulkanCommandEncoder) (Object) this).createRenderPass(descriptor);
		} finally {
			this.iris$creatingPass = null;
		}
	}

	@WrapOperation(method = "createRenderPass", at = @At(value = "INVOKE", target = "Lorg/lwjgl/vulkan/KHRDynamicRendering;vkCmdBeginRenderingKHR(Lorg/lwjgl/vulkan/VkCommandBuffer;Lorg/lwjgl/vulkan/VkRenderingInfo;)V"))
	private void iris$beginRenderPass(VkCommandBuffer command, VkRenderingInfo rendering, Operation<Void> original) {
		if (this.iris$creatingPass == null) {
			original.call(command, rendering);
		} else {
			try (var stack = MemoryStack.stackPush()) {
				((VulkanRenderBackend) Iris.getRenderBackend()).getRenderPassCache().begin(command, stack, this.iris$creatingPass);
			}
		}
	}

	@Override
	public void iris$setWorldPass(VulkanWorldCommands pass) {
		if (this.iris$worldPass != null || !pass.owns(this.currentRenderPass)) {
			throw new IllegalStateException("invalid Iris world pass ownership");
		}

		this.iris$worldPass = pass;
	}

	@Inject(method = "submitRenderPass", at = @At("HEAD"))
	private void iris$takeWorldPass(CallbackInfo ci) {
		var pass = this.iris$worldPass;
		this.iris$worldPass = null;
		this.iris$closingWorldPass = pass != null && pass.owns(this.currentRenderPass) ? pass : null;
	}

	@WrapOperation(method = "submitRenderPass", at = @At(value = "INVOKE", target = "Lorg/lwjgl/vulkan/KHRDynamicRendering;vkCmdEndRenderingKHR(Lorg/lwjgl/vulkan/VkCommandBuffer;)V"))
	private void iris$endRenderPass(VkCommandBuffer command, Operation<Void> original) {
		if (this.iris$closingWorldPass != null && this.iris$closingWorldPass.isOwned()) {
			VK10.vkCmdEndRenderPass(command);
		} else {
			original.call(command);
		}
	}

	@WrapOperation(method = "submitRenderPass", at = @At(value = "INVOKE", target = "Lcom/mojang/renderpearl/backend/vulkan/VulkanCommandEncoder;memoryBarrier(Lorg/lwjgl/system/MemoryStack;)V"))
	private void iris$finishWorldPass(VulkanCommandEncoder encoder, MemoryStack stack, Operation<Void> original) {
		var pass = this.iris$closingWorldPass;
		this.iris$closingWorldPass = null;

		if (pass == null) {
			original.call(encoder, stack);
		} else {
			pass.finish(stack);
		}
	}
}
