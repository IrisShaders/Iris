package net.coderbot.iris.texunits;

import net.minecraft.world.phys.Vec2;

public interface TextureAtlasInterface {
	void setAtlasSize(int sizeX, int sizeY);
	Vec2 getAtlasSize();
}
