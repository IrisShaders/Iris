package net.coderbot.iris.texture;

import net.coderbot.iris.vendored.joml.Vector2i;

public interface TextureAtlasInterface {
	int getWidth();

	int getHeight();

	Vector2i getSizeVector();

	int getMipLevel();

	void setWidth(int width);

	void setHeight(int height);

	void setSizeVector(Vector2i size);

	void setMipLevel(int mipLevel);
}
