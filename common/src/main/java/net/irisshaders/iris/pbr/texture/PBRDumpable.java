package net.irisshaders.iris.pbr.texture;

import net.minecraft.client.renderer.texture.Dumpable;
import net.minecraft.resources.Identifier;

public interface PBRDumpable extends Dumpable {
	Identifier getDefaultDumpLocation();
}
