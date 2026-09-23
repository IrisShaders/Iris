package net.irisshaders.iris.mixin;

import net.irisshaders.iris.Iris;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.PreferredGraphicsApi;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;

@Mixin(Options.class)
public abstract class MixinOptions_GraphicsApi {
	@Shadow
	private PreferredGraphicsApi preferredGraphicsBackendFromStartup;

	@Shadow
	public abstract OptionInstance<PreferredGraphicsApi> preferredGraphicsBackend();

	@Shadow
	public abstract void save();

	@Inject(method = "<init>", at = @At("RETURN"))
	private void iris$preferVulkan(CallbackInfo ci) {
		var config = Iris.getIrisConfig();
		if (config.hasRunVulkan()) {
			return;
		}

		this.preferredGraphicsBackend().set(PreferredGraphicsApi.VULKAN);
		this.preferredGraphicsBackendFromStartup = PreferredGraphicsApi.VULKAN;
		this.save();
		config.setHasRunVulkan(true);
		try {
			config.save();
		} catch (IOException e) {
			Iris.logger.error("failed to save Iris configuration", e);
		}
	}
}
