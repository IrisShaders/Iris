package net.coderbot.iris.mixin;

import net.coderbot.iris.Iris;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Small hook giving Iris a chance to check for keyboard input for its keybindings.
 *
 * <p>This is equivalent to the END_CLIENT_TICK event in Fabric API, but since it's a super simple mixin and we
 * only need this event (out of the many events provided by Fabric API) I've just implemented it myself. This
 * alone shaves over 60kB off the released JAR size.</p>
 */
@Mixin(Minecraft.class)
public class MixinMinecraft {
	@Shadow
	private ProfilerFiller profiler;

	@Inject(method = "tick()V", at = @At("RETURN"))
	private void iris$onTick(CallbackInfo ci) {
		this.profiler.push("iris_keybinds");

		Iris.handleKeybinds((Minecraft) (Object) this);

		this.profiler.pop();
	}
}
