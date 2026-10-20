package net.irisshaders.iris.mixin;

import com.mojang.renderpearl.backend.vulkan.VulkanDebug;
import net.irisshaders.iris.Iris;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(VulkanDebug.Enabled.class)
public class MixinVulkanDebug {
	@Inject(method = {"beginDebugGroup", "endDebugGroup"}, at = @At("HEAD"), cancellable = true)
	private void iris$gateDebugGroups(CallbackInfo ci) {
		if (!Iris.getIrisConfig().areDebugOptionsEnabled()) {
			ci.cancel();
		}
	}
}
