package net.coderbot.iris.fantastic;

import java.util.function.Function;
import net.minecraft.client.renderer.RenderType;

public interface WrappingVertexConsumerProvider {
	void setWrappingFunction(Function<RenderType, RenderType> wrappingFunction);
}
