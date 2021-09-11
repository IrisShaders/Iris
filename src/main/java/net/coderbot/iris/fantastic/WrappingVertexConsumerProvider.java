package net.coderbot.iris.fantastic;

import net.minecraft.client.render.RenderLayer;

import java.util.function.Function;

public interface WrappingVertexConsumerProvider {
	void pushWrappingFunction(Function<RenderLayer, RenderLayer> wrappingFunction);
	void popWrappingFunction();
	void assertWrapStackEmpty();
}
