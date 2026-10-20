package net.irisshaders.iris.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.commands.GpuQueryPool;
import com.mojang.renderpearl.api.commands.RenderPassDescriptor;
import com.mojang.renderpearl.api.textures.GpuTexture;
import com.mojang.renderpearl.backend.api.RenderPassBackend;
import com.mojang.renderpearl.backend.vulkan.VulkanCommandEncoder;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.mixinterface.VulkanQueryPoolAccess;
import net.irisshaders.iris.mixinterface.VulkanWorldPassAccess;
import net.irisshaders.iris.vulkan.VulkanRenderBackend;
import net.irisshaders.iris.vulkan.VulkanSynchronization;
import net.irisshaders.iris.vulkan.VulkanTextureState;
import net.irisshaders.iris.vulkan.VulkanWorldCommands;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkRenderingInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


import static org.lwjgl.vulkan.KHRSynchronization2.*;

// Minecraft owns command and pass lifetime; Iris only substitutes native begin/end for its own passes.
@Mixin(VulkanCommandEncoder.class)
public abstract class MixinVulkanCommandEncoder implements VulkanWorldPassAccess {
	@Unique
	private VulkanWorldCommands iris$worldPass;

	@Unique
	private RenderPassDescriptor iris$creatingPass;
	@Unique
	private VulkanTextureState iris$textures;

	@Override
	public void iris$setTextureState(VulkanTextureState textures) {
		this.iris$textures = textures;
	}

	@Override
	public RenderPassBackend iris$createRenderPass(RenderPassDescriptor descriptor) {
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

	@Inject(method = "createRenderPass", at = @At("HEAD"))
	private void iris$prepareRenderPass(RenderPassDescriptor descriptor, CallbackInfoReturnable<RenderPassBackend> cir) {
		if (this.iris$textures == null) {
			return;
		}
		this.iris$textures.flush();
		Iris.getRenderBackend().sync(VulkanSynchronization.GRAPHICS, VulkanSynchronization.GRAPHICS_READS,
			VulkanSynchronization.GRAPHICS_WRITES);
	}

	@Inject(method = {"submit", "destroy", "waitSemaphore", "signalSemaphore"}, at = @At("HEAD"))
	private void iris$flushCommands(CallbackInfo ci) {
		if (this.iris$textures != null) {
			this.iris$textures.flush();
		}
	}

	@WrapOperation(method = "writeTimestamp", at = @At(value = "INVOKE", target = "Lorg/lwjgl/vulkan/VK12;vkResetQueryPool(Lorg/lwjgl/vulkan/VkDevice;JII)V"))
	private void iris$resetTimestampOnQueue(VkDevice device, long pool, int first, int count, Operation<Void> original,
		@Local(argsOnly = true) GpuQueryPool queries) {
		var encoder = (VulkanCommandEncoder) (Object) this;
		// not allowed in render pass?
		VK10.vkCmdResetQueryPool(((VulkanCommandEncoderAccessor) encoder).iris$objectInitCommandBuffer(), pool, first, count);
		((VulkanQueryPoolAccess) queries).iris$markWrite(first, ((VulkanCommandEncoderAccessor) encoder).iris$getCurrentSubmitIndex());
	}

	@Inject(method = {"clearColorTextureUnsynced", "clearDepthTextureUnsynced"}, at = @At("HEAD"))
	private void iris$syncClear(CallbackInfo ci, @Local(argsOnly = true) GpuTexture texture) {
		if (this.iris$textures != null) {
			this.iris$textures.beforeAccess(texture);
			Iris.getRenderBackend().sync(VK_PIPELINE_STAGE_2_CLEAR_BIT_KHR, 0, VK_ACCESS_2_TRANSFER_WRITE_BIT_KHR);
		}
	}

	@Inject(method = {"writeToBuffer", "copyToBuffer"}, at = @At("HEAD"))
	private void iris$syncBufferTransfer(CallbackInfo ci) {
		if (this.iris$textures != null) {
			Iris.getRenderBackend().sync(VK_PIPELINE_STAGE_2_COPY_BIT_KHR, VK_ACCESS_2_TRANSFER_READ_BIT_KHR,
				VK_ACCESS_2_TRANSFER_WRITE_BIT_KHR);
		}
	}

	@Inject(method = {"writeToTexture", "copyBufferToTexture",
		"copyTextureToBuffer(Lcom/mojang/renderpearl/api/textures/GpuTexture;Lcom/mojang/renderpearl/api/buffers/GpuBuffer;JLjava/lang/Runnable;IIIII)V"},
		at = @At("HEAD"))
	private void iris$syncImageTransfer(CallbackInfo ci, @Local(argsOnly = true) GpuTexture texture) {
		if (this.iris$textures != null) {
			this.iris$textures.beforeAccess(texture);
			Iris.getRenderBackend().sync(VK_PIPELINE_STAGE_2_COPY_BIT_KHR, VK_ACCESS_2_TRANSFER_READ_BIT_KHR,
				VK_ACCESS_2_TRANSFER_WRITE_BIT_KHR);
		}
	}

	@Inject(method = "copyTextureToTexture", at = @At("HEAD"))
	private void iris$syncImageCopy(CallbackInfo ci, @Local(argsOnly = true, ordinal = 0) GpuTexture source,
		@Local(argsOnly = true, ordinal = 1) GpuTexture destination) {
		if (this.iris$textures != null) {
			this.iris$textures.beforeAccess(source);
			this.iris$textures.beforeAccess(destination);
			Iris.getRenderBackend().sync(VK_PIPELINE_STAGE_2_COPY_BIT_KHR, VK_ACCESS_2_TRANSFER_READ_BIT_KHR,
				VK_ACCESS_2_TRANSFER_WRITE_BIT_KHR);
		}
	}

	@Inject(method = "execute", at = @At("HEAD"))
	private void iris$syncExternalCommands(CallbackInfo ci) {
		if (this.iris$textures != null) {
			this.iris$textures.flush();
			Iris.getRenderBackend().sync(VK_PIPELINE_STAGE_2_ALL_COMMANDS_BIT_KHR, VK_ACCESS_2_MEMORY_READ_BIT_KHR,
				VK_ACCESS_2_MEMORY_WRITE_BIT_KHR);
		}
	}

	@Override
	public void iris$setWorldPass(VulkanWorldCommands pass) {
		this.iris$worldPass = pass;
	}

	@WrapOperation(method = "submitRenderPass", at = @At(value = "INVOKE", target = "Lorg/lwjgl/vulkan/KHRDynamicRendering;vkCmdEndRenderingKHR(Lorg/lwjgl/vulkan/VkCommandBuffer;)V"))
	private void iris$endRenderPass(VkCommandBuffer command, Operation<Void> original) {
		if (this.iris$worldPass != null && this.iris$worldPass.isOwned()) {
			VK10.vkCmdEndRenderPass(command);
		} else {
			original.call(command);
		}
	}

	@WrapOperation(method = "submitRenderPass", at = @At(value = "INVOKE", target = "Lcom/mojang/renderpearl/backend/vulkan/VulkanCommandEncoder;memoryBarrier(Lorg/lwjgl/system/MemoryStack;)V"))
	private void iris$finishWorldPass(VulkanCommandEncoder encoder, MemoryStack stack, Operation<Void> original) {
		var pass = this.iris$worldPass;
		this.iris$worldPass = null;

		if (pass == null) {
			original.call(encoder, stack);
		}
	}
}
