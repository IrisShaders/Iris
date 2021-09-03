package net.coderbot.batchedentityrendering.impl;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexFormat;

public class RenderLayerUtil {
    public static boolean isTriangleStripDrawMode(RenderLayer renderLayer) {
        return renderLayer.getDrawMode() == VertexFormat.DrawMode.TRIANGLE_STRIP;
    }
}
