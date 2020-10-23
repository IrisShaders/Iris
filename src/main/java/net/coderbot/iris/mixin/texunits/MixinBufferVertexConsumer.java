package net.coderbot.iris.mixin.texunits;

import com.mojang.blaze3d.platform.GlStateManager;
import org.spongepowered.asm.mixin.Mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Mixin(GlStateManager.class)
@Environment(EnvType.CLIENT)
public class MixinBufferVertexConsumer {
	// TODO: Fix up light() and overlay() to use the right texture unit
}
