package net.coderbot.iris.mixin;

import net.coderbot.iris.texunits.TextureAtlasInterface;
import net.minecraft.client.renderer.texture.Stitcher;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.phys.Vec2;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(TextureAtlas.class)
public class MixinTextureAtlas implements TextureAtlasInterface {
	private Vec2 atlasSize;

	@Inject(method = "getLoadedSprites", at = @At("HEAD"))
	private void getAtlasSize(ResourceManager resourceManager, Stitcher textureStitcher, int maxLevel, CallbackInfoReturnable<List<TextureAtlasSprite>> cir) {
		atlasSize = new Vec2(textureStitcher.getWidth(), textureStitcher.getHeight());
	}

	@Override
	public Vec2 getAtlasSize() {
		return atlasSize;
	}
}

