package net.coderbot.iris.compat.sodium.mixin.vertex_format;

import me.jellysquid.mods.sodium.client.render.vertex.transform.CommonVertexElement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(CommonVertexElement.class)
public interface CommonVertexElementAccessor {
	@Invoker(value = "<init>")
	static CommonVertexElement createCommonVertexElement(String name, int ordinal) {
		throw new AssertionError();
	}
}
