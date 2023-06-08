package net.coderbot.iris.mixin.devenvironment;

import com.mojang.logging.LogUtils;
import net.minecraft.SharedConstants;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

// use a higher priority to apply after LazyDFU, to avoid a conflict.
@Mixin(value = SharedConstants.class, priority = 1010)
public class MixinSharedConstants_LazyDfu {
}
