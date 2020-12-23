package net.coderbot.iris.mixin;

import com.mojang.blaze3d.platform.GlStateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(GlStateManager.class)
public class MixinGlStateManager {
	private static final int MAX_TEXTURE_IMAGE_UNITS = 32;

	@ModifyConstant(method = "<clinit>", constant = @Constant(intValue = 12), require = 1)
	private static int iris$increaseMaximumAllowedTextureUnits(int existingValue) {
		return MAX_TEXTURE_IMAGE_UNITS;
	}
}
