package net.coderbot.iris.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import net.coderbot.iris.texunits.TextureAtlasInterface;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.Stitcher;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.phys.Vec2;
import org.lwjgl.opengl.GL20C;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(TextureAtlas.class)
public abstract class MixinTextureAtlas extends AbstractTexture implements TextureAtlasInterface {
	private Vec2 atlasSize;

	@Inject(method = "getLoadedSprites", at = @At("HEAD"))
	private void getAtlasSize(ResourceManager resourceManager, Stitcher stitcher, int i, CallbackInfoReturnable<List<TextureAtlasSprite>> cir) {
		this.atlasSize = new Vec2(stitcher.getWidth(), stitcher.getHeight());
	}

	@Override
	public Vec2 getAtlasSize() {
		if (this.atlasSize == null) {
			// support for DashLoader (and other mods which might mess with the other code path)
			int glId = this.getId();

			// Keep track of what texture was bound before
			int existingGlId = GL20C.glGetInteger(GL20C.GL_TEXTURE_BINDING_2D);

			// Bind this texture and grab the atlas size from it.
			RenderSystem.bindTexture(glId);
			int width = GL20C.glGetTexLevelParameteri(GL20C.GL_TEXTURE_2D, 0, GL20C.GL_TEXTURE_WIDTH);
			int height = GL20C.glGetTexLevelParameteri(GL20C.GL_TEXTURE_2D, 0, GL20C.GL_TEXTURE_HEIGHT);
			this.atlasSize = new Vec2(width, height);

			// Make sure to re-bind the previous texture to avoid issues.
			RenderSystem.bindTexture(existingGlId);
		}

		return this.atlasSize;
	}
}

