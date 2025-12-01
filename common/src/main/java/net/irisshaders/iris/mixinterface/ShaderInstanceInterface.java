package net.irisshaders.iris.mixinterface;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.server.packs.resources.ResourceProvider;

import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.util.Map;

public interface ShaderInstanceInterface {
	void setShouldSkip(MethodHandle s);
}
