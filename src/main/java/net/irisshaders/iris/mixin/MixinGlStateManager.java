package net.irisshaders.iris.mixin;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin({GlStateManager.class, RenderSystem.class})
public class MixinGlStateManager {
	@ModifyConstant(method = "<clinit>", constant = @Constant(intValue = 12), require = 1)
	private static int iris$increaseMaximumAllowedTextureUnits(int existingValue) {
		// should be enough, I hope...
		// We can't query OpenGL for this since RenderSystem is initialized too early.
		return 128;
	}
}
