package net.irisshaders.iris.mixin;

import com.mojang.renderpearl.api.textures.GpuTexture;
import com.mojang.renderpearl.backend.common.BaseGpuTexture;
import net.irisshaders.iris.mixinterface.GpuTextureInterface;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BaseGpuTexture.class)
public abstract class MixinGpuTexture2 implements GpuTextureInterface {
	@Override
	public int iris$getGlId() {
		throw new AssertionError("Why.");
	}

	@Override
	public void iris$markMipmapNonLinear() {
		throw new AssertionError("Why.");
	}
}
