package net.coderbot.iris.mixin.texunits;

import net.coderbot.iris.texunits.TextureUnit;
import org.lwjgl.opengl.GL15;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import net.minecraft.client.render.OverlayTexture;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Mixin(OverlayTexture.class)
@Environment(EnvType.CLIENT)
public class MixinOverlayTexture {
	@ModifyConstant(method = "<init>()V", constant = @Constant(intValue = GL15.GL_TEXTURE1), require = 1)
	private int iris$fixOverlayTextureUnit(int texUnit) {
		return TextureUnit.OVERLAY.getUnitId();
	}
}
