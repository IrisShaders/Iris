package net.irisshaders.iris.mixin;

import com.mojang.blaze3d.opengl.GlTexture;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.TextureFormat;
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

	public MixinGpuTexture(int i, String string, TextureFormat textureFormat, int j, int k, int l, int m) {
		super(i, string, textureFormat, j, k, l, m);
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
