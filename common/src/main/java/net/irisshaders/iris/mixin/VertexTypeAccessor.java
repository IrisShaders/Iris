package net.irisshaders.iris.mixin;

import com.mojang.blaze3d.shaders.Program;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(VertexFormatElement.Type.class)
public interface VertexTypeAccessor {
	@Invoker(value = "<init>")
	static VertexFormatElement.Type createFormatType(String name, int ordinal, int size, String name2, int glType) {
		throw new AssertionError();
	}
}
