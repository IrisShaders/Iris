package net.irisshaders.iris.mixin;

import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.backend.common.BaseGpuTexture;
import com.mojang.renderpearl.backend.opengl.GlTexture;
import net.irisshaders.iris.mixinterface.GpuTextureInterface;
import net.irisshaders.iris.pbr.TextureTracker;
import net.irisshaders.iris.pbr.texture.PBRTextureManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GlTexture.class)
public abstract class MixinGpuTexture extends BaseGpuTexture implements GpuTextureInterface {
	@Shadow
	@Final
	protected int id;
	@Unique
	private boolean mipmapNonLinear;


	public MixinGpuTexture(@Usage int usage, String label, GpuFormat format, int width, int height, int depthOrLayers, int mipLevels) {
		super(usage, label, format, width, height, depthOrLayers, mipLevels);
	}

	@Shadow
	public abstract int glId();

	@Override
	public int iris$getGlId() {
		return this.glId();
	}

	@Override
	public void iris$markMipmapNonLinear() {
		this.mipmapNonLinear = true;
	}

	@Inject(method = "close", at = @At("HEAD"))
	private void iris$releaseTrackedTexture(CallbackInfo ci) {
		var texture = (GlTexture) (Object) this;
		TextureTracker.INSTANCE.onDeleteTexture(texture);
		PBRTextureManager.INSTANCE.onDeleteTexture(texture);
	}
}
