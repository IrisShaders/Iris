package net.coderbot.iris.shaderpack;

import net.coderbot.iris.rendertarget.NativeImageBackedCustomTexture;
import net.fabricmc.loader.api.Version;
import net.minecraft.client.renderer.texture.AbstractTexture;

public record PackMeta(String pathFileName, String readableName, Version version) {
}
