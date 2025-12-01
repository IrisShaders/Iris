package net.irisshaders.iris.mixin.texture;

import net.minecraft.client.renderer.texture.ReloadableTexture;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ReloadableTexture.class)
public interface ReloadableTextureAccessor {
	@Accessor("resourceId")
	ResourceLocation getLocation();
}
