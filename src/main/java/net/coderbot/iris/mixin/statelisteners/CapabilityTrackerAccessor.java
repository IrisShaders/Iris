package net.coderbot.iris.mixin.statelisteners;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = "com/mojang/blaze3d/platform/GlStateManager$CapabilityTracker")
public interface CapabilityTrackerAccessor {
	@Accessor("state")
	boolean getState();
}
