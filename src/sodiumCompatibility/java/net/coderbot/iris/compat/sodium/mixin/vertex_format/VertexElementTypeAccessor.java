package net.coderbot.iris.compat.sodium.mixin.vertex_format;

import com.mojang.blaze3d.vertex.VertexFormatElement;
import me.jellysquid.mods.sodium.client.render.vertex.VertexElementType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(VertexElementType.class)
public interface VertexElementTypeAccessor {
	@Invoker(value = "<init>")
	static VertexElementType createVertexElementType(String name, int ordinal, VertexFormatElement element) {
		throw new AssertionError();
	}
}
