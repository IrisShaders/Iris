package net.coderbot.iris.fantastic.ordering;

import net.minecraft.client.render.RenderLayer;

public interface RenderOrderManager {
    void begin(RenderLayer layer);
    void startGroup();
    boolean maybeStartGroup();
    void endGroup();
    void reset();
    Iterable<RenderLayer> getRenderOrder();
}
