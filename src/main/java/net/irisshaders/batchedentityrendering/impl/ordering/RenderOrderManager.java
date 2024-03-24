package net.irisshaders.batchedentityrendering.impl.ordering;

import net.irisshaders.batchedentityrendering.impl.TransparencyType;
import net.minecraft.client.renderer.RenderType;

import java.util.List;

public interface RenderOrderManager {
	void begin(RenderType type);

	void startGroup();

	boolean maybeStartGroup();

	void endGroup();

	void reset();

	void resetType(TransparencyType type);

	List<RenderType> getRenderOrder();
}
