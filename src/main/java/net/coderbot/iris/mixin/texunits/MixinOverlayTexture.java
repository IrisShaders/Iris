package net.coderbot.iris.mixin.texunits;

import net.coderbot.iris.texunits.TextureUnit;
import org.lwjgl.opengl.GL15;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.texture.OverlayTexture;

@Mixin(OverlayTexture.class)
@Environment(EnvType.CLIENT)
public class MixinOverlayTexture {
	@ModifyConstant(method = "<init>()V", constant = @Constant(intValue = GL15.GL_TEXTURE1), require = 1)
	private int iris$fixOverlayTextureUnit(int texUnit) {
		return TextureUnit.OVERLAY.getUnitId();
	}
}
