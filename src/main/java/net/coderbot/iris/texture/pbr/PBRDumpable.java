package net.coderbot.iris.texture.pbr;

import net.minecraft.client.renderer.texture.Dumpable;
import net.minecraft.resources.ResourceLocation;

public interface PBRDumpable extends Dumpable {
	ResourceLocation getDefaultDumpLocation();
}
