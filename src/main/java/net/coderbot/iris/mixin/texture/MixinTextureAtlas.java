package net.coderbot.iris.mixin.texture;

import net.coderbot.iris.texture.AtlasInfoGatherer;
import net.coderbot.iris.texture.TextureAtlasInterface;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.server.packs.resources.ResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TextureAtlas.class)
public abstract class MixinTextureAtlas extends AbstractTexture implements TextureAtlasInterface {
	@Unique
	private int width = -1;

	@Unique
	private int height = -1;

	@Unique
	private int mipLevel = -1;

	@Override
	public int getWidth() {
		return width;
	}

	@Override
	public int getHeight() {
		return height;
	}

	@Override
	public int getMipLevel() {
		return mipLevel;
	}

	@Override
	public void setWidth(int width) {
		this.width = width;
	}

	@Override
	public void setHeight(int height) {
		this.height = height;
	}

	@Override
	public void setMipLevel(int mipLevel) {
		this.mipLevel = mipLevel;
	}

	@Inject(method = "load(Lnet/minecraft/server/packs/resources/ResourceManager;)V", at = @At("TAIL"))
	private void onTailLoad(ResourceManager resourceManager, CallbackInfo ci) {
		AtlasInfoGatherer.resetInfo((TextureAtlas) (Object) this);
	}
}
