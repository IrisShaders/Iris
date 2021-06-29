package net.coderbot.iris.mixin;

import net.coderbot.iris.Iris;
import net.coderbot.iris.pipeline.ShadowRenderer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.hud.DebugHud;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(DebugHud.class)
public abstract class MixinDebugHud {
    @Inject(method = "getRightText", at = @At("RETURN"))
    private void appendShaderPackText(CallbackInfoReturnable<List<String>> cir) {
        List<String> messages = cir.getReturnValue();

        messages.add("");
        messages.add("[Iris] Version: " + Iris.getFormattedVersion());
        messages.add("");
        messages.add("[Iris] Shaderpack: " + Iris.getCurrentPackName());
    }

	@Inject(method = "getLeftText", at = @At("RETURN"))
	private void appendShadowDebugText(CallbackInfoReturnable<List<String>> cir) {
		List<String> messages = cir.getReturnValue();

		if (!FabricLoader.getInstance().isModLoaded("sodium") && Iris.getCurrentPack().isPresent()) {
			messages.add(1, Formatting.YELLOW + "[Iris] Sodium isn't installed; you will have poor performance.");
			messages.add(2, Formatting.YELLOW + "[Iris] Install the compatible Sodium fork if you want to run benchmarks or get higher FPS!");
		}

		Iris.getPipelineManager().getPipeline().addDebugText(messages);
	}
}
