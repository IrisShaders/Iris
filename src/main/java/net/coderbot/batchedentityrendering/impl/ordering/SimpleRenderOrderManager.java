package net.coderbot.batchedentityrendering.impl.ordering;

import net.minecraft.client.renderer.RenderType;

import java.util.LinkedHashSet;

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

    public Iterable<RenderType> getRenderOrder() {
        return renderTypes;
    }
}
