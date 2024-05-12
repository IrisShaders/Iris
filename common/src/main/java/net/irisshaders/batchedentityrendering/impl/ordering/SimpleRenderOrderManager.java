package net.irisshaders.batchedentityrendering.impl.ordering;

import net.irisshaders.batchedentityrendering.impl.TransparencyType;
import net.minecraft.client.renderer.RenderType;

import java.util.LinkedHashSet;
import java.util.List;

public class SimpleRenderOrderManager implements RenderOrderManager {
	private final LinkedHashSet<RenderType> renderTypes;

	public SimpleRenderOrderManager() {
		renderTypes = new LinkedHashSet<>();
	}

	public void begin(RenderType type) {
		renderTypes.add(type);
	}

	public void startGroup() {
		// no-op
	}

	public boolean maybeStartGroup() {
		// no-op
		return false;
	}

	public void endGroup() {
		// no-op
	}

	@Override
	public void reset() {
		renderTypes.clear();
	}

	@Override
	public void resetType(TransparencyType type) {

	}

	public List<RenderType> getRenderOrder() {
		return List.copyOf(renderTypes);
	}
}
