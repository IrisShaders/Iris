package net.coderbot.iris.fantastic;

import net.minecraft.client.renderer.RenderType;

import java.util.function.Function;

public interface WrappingVertexConsumerProvider {
	void pushWrappingFunction(Function<RenderType, RenderType> wrappingFunction);
	void popWrappingFunction();
	void assertWrapStackEmpty();
}
