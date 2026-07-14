package net.irisshaders.iris.pathways.colorspace;

import com.mojang.renderpearl.backend.opengl.GlTexture;

public interface ColorSpaceConverter {
	void rebuildProgram(int width, int height, ColorSpace colorSpace);

	void process(GlTexture target);
}
