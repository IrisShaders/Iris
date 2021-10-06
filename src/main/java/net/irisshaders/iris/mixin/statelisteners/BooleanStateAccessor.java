package net.irisshaders.iris.mixin.statelisteners;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = "com/mojang/blaze3d/platform/GlStateManager$BooleanState")
public interface BooleanStateAccessor {
	@Accessor("enabled")
	boolean isEnabled();
}
