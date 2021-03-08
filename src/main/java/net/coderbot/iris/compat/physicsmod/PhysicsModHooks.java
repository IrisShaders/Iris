package net.coderbot.iris.compat.physicsmod;

import net.coderbot.iris.layer.GbufferProgram;
import net.coderbot.iris.layer.GbufferPrograms;
import net.minecraft.client.render.RenderLayer;

public class PhysicsModHooks {
	public static void redirectEndDrawing(RenderLayer layer) {
		GbufferPrograms.push(GbufferProgram.TEXTURED_LIT);
	}

	public static void redirectStartDrawing(RenderLayer layer) {
		if (layer == getTargetRenderLayer()) {
			GbufferPrograms.pop(GbufferProgram.TEXTURED_LIT);
		}
	}

	public static RenderLayer getTargetRenderLayer() {
		return RenderLayer.getCutout();
	}
}
