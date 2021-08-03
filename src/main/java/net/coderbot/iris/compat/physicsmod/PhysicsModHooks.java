package net.coderbot.iris.compat.physicsmod;

import net.coderbot.iris.layer.GbufferProgram;
import net.coderbot.iris.layer.GbufferPrograms;
import net.minecraft.client.renderer.RenderType;

public class PhysicsModHooks {
	public static void redirectEndDrawing(RenderType layer) {
		GbufferPrograms.push(GbufferProgram.TEXTURED_LIT);
	}

	public static void redirectStartDrawing(RenderType layer) {
		if (layer == getTargetRenderLayer()) {
			GbufferPrograms.pop(GbufferProgram.TEXTURED_LIT);
		}
	}

	public static RenderType getTargetRenderLayer() {
		return RenderType.cutout();
	}
}
