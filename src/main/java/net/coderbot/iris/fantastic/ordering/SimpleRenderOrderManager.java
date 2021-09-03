package net.coderbot.iris.fantastic.ordering;

import net.minecraft.client.render.RenderLayer;

import java.util.LinkedHashSet;
import java.util.List;

public class SimpleRenderOrderManager implements RenderOrderManager {
    private final LinkedHashSet<RenderLayer> layers;

    public SimpleRenderOrderManager() {
        layers = new LinkedHashSet<>();
    }

    public void begin(RenderLayer layer) {
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

    public Iterable<RenderLayer> getRenderOrder() {
        return layers;
    }
}
