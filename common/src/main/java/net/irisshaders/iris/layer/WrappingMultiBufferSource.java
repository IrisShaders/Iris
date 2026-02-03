package net.irisshaders.iris.layer;

import net.minecraft.client.renderer.rendertype.RenderType;

import java.util.function.Function;

public interface WrappingMultiBufferSource {
	void pushWrappingFunction(Function<RenderType, RenderType> wrappingFunction);

	void popWrappingFunction();

	void assertWrapStackEmpty();
}
