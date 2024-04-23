package net.irisshaders.iris.mixin;

import net.irisshaders.iris.Iris;
import net.minecraft.SystemReport;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Supplier;

/**
 * Adds the current shaderpack and number of changed options to crash reports
 */
@Mixin(SystemReport.class)
public abstract class MixinSystemReport {
	@Shadow
	public abstract void setDetail(String string, Supplier<String> supplier);

	@Inject(at = @At("RETURN"), method = "<init>")
	private void fillSystemDetails(CallbackInfo ci) {
		if (Iris.getCurrentPackName() == null) return; // this also gets called at startup for some reason

		this.setDetail("Loaded Shaderpack", () -> {
			StringBuilder sb = new StringBuilder(Iris.getCurrentPackName() + (Iris.isFallback() ? " (fallback)" : ""));
			Iris.getCurrentPack().ifPresent(pack -> {
				sb.append("\n\t\t");
				sb.append(pack.getProfileInfo());
			});
			return sb.toString();
		});
	}
}
