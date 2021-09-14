package net.coderbot.batchedentityrendering.impl;

import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderType;

public class RenderLayerUtil {
    public static boolean isTriangleStripDrawMode(RenderType renderLayer) {
        return renderLayer.mode() == VertexFormat.Mode.TRIANGLE_STRIP;
    }
}
