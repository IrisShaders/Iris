package net.irisshaders.iris.mixin;

import net.minecraft.client.renderer.blockentity.BannerRenderer;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BannerRenderer.class)
public abstract class BannerRendererMixin {
	// maDU59_ was here =D
	// Banner patterns do not need to be rendered during the shadow pass as they are not visible anyway

}
