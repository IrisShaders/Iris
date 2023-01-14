package net.coderbot.iris.shaderpack;

import net.coderbot.iris.gl.texture.InternalTextureFormat;
import net.coderbot.iris.gl.texture.PixelFormat;
import net.coderbot.iris.gl.texture.PixelType;
import net.coderbot.iris.gl.texture.TextureType;

public record ImageInformation(String name, String samplerName, TextureType target, PixelFormat format, InternalTextureFormat internalTextureFormat,
							   PixelType type, int width, int height, int depth, boolean isRelative, float relativeWidth, float relativeHeight) {
}
