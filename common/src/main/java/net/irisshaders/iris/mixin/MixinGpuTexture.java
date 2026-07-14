package net.irisshaders.iris.mixin;

import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.backend.opengl.GlTexture;
import com.mojang.renderpearl.api.textures.GpuTexture;
import net.irisshaders.iris.gl.IrisRenderSystem;
import net.irisshaders.iris.mixinterface.GpuTextureInterface;
import org.lwjgl.opengl.GL46C;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(GlTexture.class)
public abstract class MixinGpuTexture extends GpuTexture implements GpuTextureInterface {
	@Unique
	private boolean mipmapNonLinear;

	public MixinGpuTexture(@Usage int usage, String label, GpuFormat format, int width, int height, int depthOrLayers, int mipLevels) {
		super(usage, label, format, width, height, depthOrLayers, mipLevels);
	}


	@Shadow
	public abstract int glId();

	@Shadow
	@Final
	protected int id;

	@Override
	public int iris$getGlId() {
		return this.glId();
	}

	@Override
	public void iris$markMipmapNonLinear() {
		this.mipmapNonLinear = true;
	}

}
