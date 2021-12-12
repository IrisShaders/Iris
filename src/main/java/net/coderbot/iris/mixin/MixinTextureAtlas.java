package net.coderbot.iris.mixin;

import net.coderbot.iris.samplers.TextureAtlasTracker;
import net.coderbot.iris.texunits.TextureAtlasInterface;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.world.phys.Vec2;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(TextureAtlas.class)
public abstract class MixinTextureAtlas extends AbstractTexture implements TextureAtlasInterface {
	@Unique
	private Vec2 atlasSize;

	@Override
	public int getId() {
		int id = super.getId();

		TextureAtlasTracker.INSTANCE.trackAtlas(id, (TextureAtlas) (Object) this);

		return id;
	}

	@Override
	public void setAtlasSize(int sizeX, int sizeY) {
		if (sizeX == 0 && sizeY == 0) {
			this.atlasSize = Vec2.ZERO;
		} else {
			this.atlasSize = new Vec2(sizeX, sizeY);
		}
	}

	@Override
	public Vec2 getAtlasSize() {
		return this.atlasSize;
	}
}

