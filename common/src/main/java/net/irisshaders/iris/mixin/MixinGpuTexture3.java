package net.irisshaders.iris.mixin;

import com.mojang.renderpearl.api.textures.GpuTexture;
import net.irisshaders.iris.mixinterface.GpuTextureInterface;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(GpuTexture.class)
public interface MixinGpuTexture3 extends GpuTextureInterface {

}
