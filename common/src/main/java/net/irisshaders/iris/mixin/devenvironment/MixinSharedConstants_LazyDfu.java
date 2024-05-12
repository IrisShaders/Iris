package net.irisshaders.iris.mixin.devenvironment;

import net.minecraft.SharedConstants;
import org.spongepowered.asm.mixin.Mixin;

// use a higher priority to apply after LazyDFU, to avoid a conflict.
@Mixin(value = SharedConstants.class, priority = 1010)
public class MixinSharedConstants_LazyDfu {
}
