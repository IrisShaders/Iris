package net.irisshaders.iris.backend;

import net.irisshaders.iris.gl.texture.TextureType;

import java.nio.ByteBuffer;

public interface IrisImage extends AutoCloseable {
	TextureType getType();

	int getWidth();

	int getHeight();

	int getDepth();

	long getView();

	long getImage();

	void upload(ByteBuffer pixels);

	void clear();

	@Override
	void close();
}
