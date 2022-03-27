package net.coderbot.iris.compat.sodium.impl.block_id;

import net.coderbot.iris.block_rendering.MaterialIdHolder;

public interface MaterialIdAwareVertexWriter {
    void iris$setIdHolder(MaterialIdHolder holder);

    void copyQuadAndFlipNormal();
}
