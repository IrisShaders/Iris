package net.irisshaders.iris.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import com.mojang.renderpearl.api.commands.RenderPassDescriptor;
import com.mojang.renderpearl.api.textures.GpuTexture;
import com.mojang.renderpearl.backend.api.RenderPassBackend;
import com.mojang.renderpearl.backend.vulkan.VulkanCommandEncoder;
import com.mojang.renderpearl.backend.vulkan.VulkanRenderPass;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.mixinterface.VulkanWorldPassAccess;
import net.irisshaders.iris.vulkan.VulkanRenderBackend;
import net.irisshaders.iris.vulkan.VulkanTextureState;
import net.irisshaders.iris.vulkan.VulkanWorldCommands;
import org.joml.Vector4fc;
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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.nio.ByteBuffer;

@Mixin(VulkanCommandEncoder.class)
public abstract class MixinVulkanCommandEncoder implements VulkanWorldPassAccess {
	@Shadow
	public VulkanRenderPass currentRenderPass;
	@Unique
	private VulkanWorldCommands iris$worldPass;
	@Unique
	private VulkanWorldCommands iris$closingWorldPass;
	@Unique
	private VulkanWorldCommands iris$heldPass;
	@Unique
	private boolean iris$reusedPass;

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
				this.iris$reusedPass = this.iris$heldPass != null && this.iris$heldPass.matches(this.iris$creatingPass);
				if (this.iris$reusedPass) {
					this.iris$heldPass = null;
				} else {
					((VulkanRenderBackend) Iris.getRenderBackend()).getRenderPassCache().begin(command, stack, this.iris$creatingPass);
				}
			}
		}
	}

	@Override
	public void iris$flushRenderPass() {
		var pass = this.iris$heldPass;
		this.iris$heldPass = null;
		if (pass != null) {
			pass.finishHeld();
		}
	}

	@Inject(method = "createRenderPass", at = @At("HEAD"))
	private void iris$prepareRenderPass(RenderPassDescriptor descriptor, CallbackInfoReturnable<RenderPassBackend> cir) {
		if (this.iris$textures == null) {
			return;
		}
		if (this.iris$creatingPass == null) {
			this.iris$flushRenderPass();
			this.iris$textures.flush((VulkanCommandEncoder) (Object) this);
		} else if (this.iris$heldPass != null && !this.iris$heldPass.matches(descriptor)) {
			this.iris$flushRenderPass();
		}
		this.iris$textures.written(descriptor);
	}

	@Inject(method = {"submit", "destroy", "waitSemaphore", "execute", "signalSemaphore"}, at = @At("HEAD"))
	private void iris$flushCommands(CallbackInfo ci) {
		this.iris$flushRenderPass();
		if (this.iris$textures != null) {
			this.iris$textures.flush((VulkanCommandEncoder) (Object) this);
			this.iris$textures.invalidateMipmaps();
		}
	}

	@Inject(method = {"writeToBuffer", "copyToBuffer", "writeTimestamp"}, at = @At("HEAD"))
	private void iris$flushHeldCommands(CallbackInfo ci) {
		this.iris$flushRenderPass();
	}

	@Inject(method = "clearColorTextureUnsynced", at = @At("HEAD"))
	private void iris$clearColor(MemoryStack stack, GpuTexture texture, Vector4fc color, CallbackInfo ci) {
		if (this.iris$textures != null) {
			this.iris$textures.beforeAccess((VulkanCommandEncoder) (Object) this, texture, true);
		}
	}

	@Inject(method = "clearDepthTextureUnsynced", at = @At("HEAD"))
	private void iris$clearDepth(MemoryStack stack, GpuTexture texture, double depth, CallbackInfo ci) {
		if (this.iris$textures != null) {
			this.iris$textures.beforeAccess((VulkanCommandEncoder) (Object) this, texture, true);
		}
	}

	@Inject(method = "writeToTexture", at = @At("HEAD"))
	private void iris$writeTexture(GpuTexture texture, ByteBuffer source, int mipLevel, int layer, int x, int y, int width, int height,
		CallbackInfo ci) {
		if (this.iris$textures != null) {
			this.iris$textures.beforeAccess((VulkanCommandEncoder) (Object) this, texture, true);
		}
	}

	@Inject(method = "copyBufferToTexture", at = @At("HEAD"))
	private void iris$copyBufferTexture(GpuBufferSlice source, int sourceX, int sourceY, int sourceWidth, int sourceHeight,
		GpuTexture destination, int x, int y, int width, int height, int mipLevel, int layer, CallbackInfo ci) {
		if (this.iris$textures != null) {
			this.iris$textures.beforeAccess((VulkanCommandEncoder) (Object) this, destination, true);
		}
	}

	@Inject(method = "copyTextureToTexture", at = @At("HEAD"))
	private void iris$copyTextures(GpuTexture source, GpuTexture destination, int mipLevel, int destinationX, int destinationY,
		int sourceX, int sourceY, int width, int height, CallbackInfo ci) {
		if (this.iris$textures != null) {
			this.iris$textures.beforeAccess((VulkanCommandEncoder) (Object) this, source, false);
			this.iris$textures.beforeAccess((VulkanCommandEncoder) (Object) this, destination, true);
		}
	}

	@Inject(method = "copyTextureToBuffer(Lcom/mojang/renderpearl/api/textures/GpuTexture;Lcom/mojang/renderpearl/api/buffers/GpuBuffer;JLjava/lang/Runnable;IIIII)V", at = @At("HEAD"))
	private void iris$readTexture(GpuTexture source, GpuBuffer destination, long offset, Runnable callback, int mipLevel, int x,
		int y, int width, int height, CallbackInfo ci) {
		if (this.iris$textures != null) {
			this.iris$textures.beforeAccess((VulkanCommandEncoder) (Object) this, source, false);
		}
	}

	@Override
	public void iris$setWorldPass(VulkanWorldCommands pass) {
		if (this.iris$worldPass != null || !pass.owns(this.currentRenderPass)) {
			throw new IllegalStateException("invalid Iris world pass ownership");
		}

		pass.reused(pass.isOwned() && this.iris$reusedPass);
		this.iris$reusedPass = false;
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
			if (this.iris$closingWorldPass.canReuse()) {
				this.iris$heldPass = this.iris$closingWorldPass;
			} else {
				VK10.vkCmdEndRenderPass(command);
			}
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
		} else if (this.iris$heldPass != pass) {
			pass.finish(stack);
		}
	}
}
