package net.coderbot.iris.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import net.coderbot.iris.texunits.SpriteAtlasTextureInterface;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.texture.TextureStitcher;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.math.Vec2f;
import org.lwjgl.opengl.GL20C;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(SpriteAtlasTexture.class)
public abstract class MixinSpriteAtlasTexture extends AbstractTexture implements SpriteAtlasTextureInterface {
	private Vec2f atlasSize;

	@Inject(method = "loadSprites(Lnet/minecraft/resource/ResourceManager;Lnet/minecraft/client/texture/TextureStitcher;I)Ljava/util/List;", at = @At("HEAD"))
	private void getAtlasSize(ResourceManager resourceManager, TextureStitcher textureStitcher, int maxLevel, CallbackInfoReturnable<List<Sprite>> cir) {
		this.atlasSize = new Vec2f(textureStitcher.getWidth(), textureStitcher.getHeight());
	}

	@Override
	public Vec2f getAtlasSize() {
		if (this.atlasSize == null) {
			// support for DashLoader (and other mods which might mess with the other code path)
			int glId = this.getGlId();

			// Keep track of what texture was bound before
			int existingGlId = GL20C.glGetInteger(GL20C.GL_TEXTURE_BINDING_2D);

			// Bind this texture and grab the atlas size from it.
			RenderSystem.bindTexture(glId);
			int width = GL20C.glGetTexLevelParameteri(GL20C.GL_TEXTURE_2D, 0, GL20C.GL_TEXTURE_WIDTH);
			int height = GL20C.glGetTexLevelParameteri(GL20C.GL_TEXTURE_2D, 0, GL20C.GL_TEXTURE_HEIGHT);
			this.atlasSize = new Vec2f(width, height);

			// Make sure to re-bind the previous texture to avoid issues.
			RenderSystem.bindTexture(existingGlId);
		}

		return this.atlasSize;
	}
}

