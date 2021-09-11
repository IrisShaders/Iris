package net.coderbot.batchedentityrendering.impl.ordering;

import net.minecraft.client.renderer.RenderType;

import java.util.LinkedHashSet;

public class SimpleRenderOrderManager implements RenderOrderManager {
    private final LinkedHashSet<RenderType> layers;

    public SimpleRenderOrderManager() {
        layers = new LinkedHashSet<>();
    }

    public void begin(RenderType layer) {
        layers.add(layer);
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
        layers.clear();
    }

    public Iterable<RenderType> getRenderOrder() {
        return layers;
    }
}
