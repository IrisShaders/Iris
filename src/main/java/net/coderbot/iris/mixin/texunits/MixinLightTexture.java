package net.coderbot.iris.mixin.texunits;

import net.coderbot.iris.texunits.TextureUnit;
import net.minecraft.client.renderer.LightTexture;
import org.lwjgl.opengl.GL15;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Modifies {@link LightTexture} to use a configurable texture unit, instead of being hardcoded to texture
 * unit #2.
 */
@Mixin(LightTexture.class)
public class MixinLightTexture {
	@ModifyConstant(method = "turnOffLightLayer", constant = @Constant(intValue = GL15.GL_TEXTURE2), require = 1)
	private int iris$fixLightmapTextureUnit$disable(int texUnit) {
		return TextureUnit.LIGHTMAP.getUnitId();
	}

	@ModifyConstant(method = "turnOnLightLayer", constant = @Constant(intValue = GL15.GL_TEXTURE2), require = 1)
	private int iris$fixLightmapTextureUnit$enable(int texUnit) {
		return TextureUnit.LIGHTMAP.getUnitId();
	}
}
