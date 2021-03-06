package net.coderbot.iris.shaderpack;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.coderbot.iris.gl.texture.InternalTextureFormat;

public class PackDirectives {
	// TODO: These are currently hardcoded to work with Sildur's. They will need to be properly parsed from shaders.properties.
	// Some of these values also come from individual shader files, such as the requested buffer formats.

	public IntList getBuffersToBeCleared() {
		// TODO: Hardcoding for sildurs: should clear all buffers unless otherwise specified, but we skip buffer 7
		return new IntArrayList(new int[]{0, 1, 2, 3, 4, 5, 6});
	}

	public InternalTextureFormat[] getRequestedBufferFormats() {
		// TODO: This is hardcoded to use Sildur's requested buffer formats. We need to properly parse the format
		// directives from the shaderpack.
		// TODO: Don't create render targets if they are unused
		return new InternalTextureFormat[]{
			InternalTextureFormat.RGBA16,
			// TODO: Only use RGBA32F if gdepth is explicitly specified as opposed to colortex1
			InternalTextureFormat.RGB10_A2,
			InternalTextureFormat.RGBA16,
			InternalTextureFormat.R11F_G11F_B10F,
			InternalTextureFormat.RGBA16,
			InternalTextureFormat.R11F_G11F_B10F,
			InternalTextureFormat.R11F_G11F_B10F,
			InternalTextureFormat.R11F_G11F_B10F
		};
	}
}
