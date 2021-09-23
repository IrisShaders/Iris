package net.coderbot.iris.compat.physicsmod;

import net.coderbot.iris.layer.GbufferProgram;
import net.coderbot.iris.layer.GbufferPrograms;
import net.minecraft.client.renderer.RenderType;

public class PhysicsModHooks {
	public static void redirectClearRenderState(RenderType type) {
		GbufferPrograms.push(GbufferProgram.TEXTURED_LIT);
	}

	public static void redirectSetupRenderState(RenderType type) {
		if (type == getTargetRenderType()) {
			GbufferPrograms.pop(GbufferProgram.TEXTURED_LIT);
		}
	}

	public static RenderType getTargetRenderType() {
		return RenderType.cutout();
	}
}
