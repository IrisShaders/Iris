package net.coderbot.iris.mixin.texture.pbr;

import net.coderbot.iris.texture.pbr.PBRAtlasHolder;
import net.coderbot.iris.texture.pbr.TextureAtlasExtension;
import net.coderbot.iris.texture.pbr.loader.AtlasPBRLoader;
import net.coderbot.iris.texture.pbr.loader.PBRTextureLoaderRegistry;
import net.coderbot.iris.texture.util.TextureExporter;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.SpriteLoader;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TextureAtlas.class)
public abstract class MixinTextureAtlas extends AbstractTexture implements TextureAtlasExtension {
	@Shadow
	@Final
	private ResourceLocation location;
	@Shadow
	private int height;
	@Shadow
	private int width;
	@Unique
	private PBRAtlasHolder pbrHolder;

	@Unique
	private SpriteLoader.Preparations prep;

	@Inject(method = "cycleAnimationFrames()V", at = @At("TAIL"))
	private void iris$onTailCycleAnimationFrames(CallbackInfo ci) {
		if (pbrHolder != null) {
			pbrHolder.cycleAnimationFrames();
		}
	}

	@Inject(method = "clearTextureData", at = @At("TAIL"))
	private void clearTextureSettingPBR(CallbackInfo ci) {
		((AtlasPBRLoader) PBRTextureLoaderRegistry.INSTANCE.getLoader(TextureAtlas.class)).removeAtlasInformation(((TextureAtlas) (Object) this));
	}

	@Inject(method = "upload", at = @At("TAIL"))
	private void export(SpriteLoader.Preparations arg, CallbackInfo ci) {
		this.prep = arg;
		/*if (pbrHolder != null) {
			if (pbrHolder.getNormalAtlas() != null) {
				pbrHolder.getNormalAtlas().upload(arg.width(), arg.height(), arg.mipLevel());
			}

			if (pbrHolder.getSpecularAtlas() != null) {
				pbrHolder.getSpecularAtlas().upload(arg.width(), arg.height(), arg.mipLevel());
			}
		}*/

		//TextureExporter.exportTextures("pbr_debug/atlas", location.getNamespace() + "_" + location.getPath().replaceAll("/", "_"), this.getId(), arg.mipLevel(), arg.width(), arg.height());
	}

	@Override
	public PBRAtlasHolder getPBRHolder() {
		return pbrHolder;
	}

	@Override
	public PBRAtlasHolder getOrCreatePBRHolder() {
		if (pbrHolder == null) {
			pbrHolder = new PBRAtlasHolder();
		}
		return pbrHolder;
	}

	@Override
	public int getWidth() {
		return this.width;
	}

	@Override
	public int getHeight() {
		return this.height;
	}
}
