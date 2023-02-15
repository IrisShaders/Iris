package net.coderbot.iris.compat.sodium.mixin.vertex_format;

import com.mojang.blaze3d.vertex.VertexFormatElement;
import net.caffeinemc.mods.sodium.api.vertex.attributes.CommonVertexAttribute;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(CommonVertexAttribute.class)
public interface VertexElementTypeAccessor {
	@Invoker(value = "<init>")
	static CommonVertexAttribute createVertexElementType(String name, int ordinal, VertexFormatElement element) {
		throw new AssertionError();
	}
}
