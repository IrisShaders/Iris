package net.irisshaders.iris.mixin;

import com.mojang.blaze3d.pipeline.TextureTarget;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LightTexture.class)
public interface LightTextureAccessor {
	@Accessor("target")
	TextureTarget getLightTexture();
}
