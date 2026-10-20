package net.irisshaders.iris.mixin;

import com.mojang.renderpearl.backend.vulkan.VulkanDevice;
import com.mojang.renderpearl.backend.vulkan.VulkanQueryPool;
import net.irisshaders.iris.mixinterface.VulkanQueryPoolAccess;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Arrays;
import java.util.OptionalLong;

@Mixin(VulkanQueryPool.class)
public class MixinVulkanQueryPool implements VulkanQueryPoolAccess {
	@Shadow
	@Final
	private VulkanDevice device;
	@Unique
	private long[] iris$submissions;

	@Inject(method = "<init>", at = @At("TAIL"))
	private void iris$initializeSubmissions(VulkanDevice device, int size, CallbackInfo ci) {
		this.iris$submissions = new long[size];
	}

	@Override
	public void iris$markWrite(int index, long submission) {
		this.iris$submissions[index] = submission;
	}

	@Inject(method = "getValues", at = @At("HEAD"), cancellable = true)
	private void iris$awaitWrittenSubmission(int index, int count, CallbackInfoReturnable<OptionalLong[]> cir) {
		long completed = ((VulkanCommandEncoderAccessor) this.device.createCommandEncoder()).iris$getCompletedSubmitIndex();
		for (int i = index; i < index + count; i++) {
			if (this.iris$submissions[i] > completed) {
				// A queued reset has not executed yet, so availability may still describe the previous write.
				var pending = new OptionalLong[count];
				Arrays.fill(pending, OptionalLong.empty());
				cir.setReturnValue(pending);
				return;
			}
		}
	}
}
