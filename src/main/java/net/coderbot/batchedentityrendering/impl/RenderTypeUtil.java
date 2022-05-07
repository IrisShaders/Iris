package net.coderbot.batchedentityrendering.impl;

import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderType;

public class RenderTypeUtil {
    public static boolean isTriangleStripDrawMode(RenderType renderType) {
        return renderType.mode() == VertexFormat.Mode.TRIANGLE_STRIP;
    }
}
