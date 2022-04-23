package net.coderbot.iris.mixin.texunits;

import net.coderbot.iris.texunits.TextureUnit;
import net.minecraft.client.renderer.texture.OverlayTexture;
import org.lwjgl.opengl.GL15;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(OverlayTexture.class)
public class MixinOverlayTexture {
	@ModifyConstant(method = "<init>()V", constant = @Constant(intValue = GL15.GL_TEXTURE1), require = 1)
	private int iris$fixOverlayTextureUnit(int texUnit) {
		return TextureUnit.OVERLAY.getUnitId();
	}
}
