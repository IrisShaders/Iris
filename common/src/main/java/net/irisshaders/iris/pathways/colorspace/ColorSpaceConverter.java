package net.irisshaders.iris.pathways.colorspace;

import com.mojang.renderpearl.api.textures.GpuTextureView;

public interface ColorSpaceConverter extends AutoCloseable {
	@Override
	default void close() {
	}

	void rebuildProgram(int width, int height, ColorSpace colorSpace);

	default void prepare(GpuTextureView target) {
	}

	void process(GpuTextureView target);
}
