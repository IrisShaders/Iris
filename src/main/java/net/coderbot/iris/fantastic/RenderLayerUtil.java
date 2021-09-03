package net.coderbot.iris.fantastic;

import net.minecraft.client.render.RenderLayer;
import org.lwjgl.opengl.GL11C;

public class RenderLayerUtil {
    public static boolean isTriangleStripDrawMode(RenderLayer renderLayer) {
        return renderLayer.getDrawMode() == GL11C.GL_TRIANGLE_STRIP;
    }
}
