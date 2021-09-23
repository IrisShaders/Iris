package net.coderbot.batchedentityrendering.impl.ordering;

import net.minecraft.client.renderer.RenderType;

public interface RenderOrderManager {
    void begin(RenderType type);
    void startGroup();
    boolean maybeStartGroup();
    void endGroup();
    void reset();
    Iterable<RenderType> getRenderOrder();
}
