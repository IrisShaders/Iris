package net.coderbot.iris.mixin.texunits;

import com.mojang.blaze3d.vertex.BufferVertexConsumer;
import net.coderbot.iris.texunits.TextureUnit;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(BufferVertexConsumer.class)
@Environment(EnvType.CLIENT)
public interface MixinBufferVertexConsumer {
	@ModifyConstant(method = "overlayCoords", constant = @Constant(intValue = 1), require = 1)
	default int iris$fixOverlayIndex(int index) {
		return TextureUnit.OVERLAY.getSamplerId();
	}

	@ModifyConstant(method = "uv2", constant = @Constant(intValue = 2), require = 1)
	default int iris$fixLightIndex(int index) {
		return TextureUnit.LIGHTMAP.getSamplerId();
	}
}
