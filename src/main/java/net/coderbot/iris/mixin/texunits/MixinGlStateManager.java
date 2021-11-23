package net.coderbot.iris.mixin.texunits;

import com.mojang.blaze3d.platform.GlStateManager;
import net.coderbot.iris.texunits.TextureUnit;
import org.lwjgl.opengl.GL15;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Mixin(GlStateManager.class)
@Environment(EnvType.CLIENT)
public class MixinGlStateManager {
	@ModifyConstant(method = "setupOverlayColor(II)V", constant = @Constant(intValue = GL15.GL_TEXTURE1), require = 1)
	private static int iris$fixOverlayTextureUnit(int texUnit) {
		return TextureUnit.OVERLAY.getUnitId();
	}

	@ModifyConstant(method = "teardownOverlayColor()V", constant = @Constant(intValue = GL15.GL_TEXTURE1), require = 1)
	private static int iris$fixOverlayTextureUnitTeardown(int texUnit) {
		return TextureUnit.OVERLAY.getUnitId();
	}
}
