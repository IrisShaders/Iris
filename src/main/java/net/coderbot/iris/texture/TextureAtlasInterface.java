package net.coderbot.iris.texture;

public interface TextureAtlasInterface {
	int getWidth();

	int getHeight();

	int getMipLevel();

	void setWidth(int width);

	void setHeight(int height);

	void setMipLevel(int mipLevel);
}
