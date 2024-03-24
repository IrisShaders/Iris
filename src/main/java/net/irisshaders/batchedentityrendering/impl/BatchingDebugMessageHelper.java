package net.irisshaders.batchedentityrendering.impl;

public class BatchingDebugMessageHelper {
	public static String getDebugMessage(DrawCallTrackingRenderBuffers drawTracker) {
		int drawCalls = drawTracker.getDrawCalls();
		int renderTypes = drawTracker.getRenderTypes();

		if (drawCalls > 0) {
			int effectivenessTimes10 = renderTypes * 1000 / drawCalls;
			float effectiveness = effectivenessTimes10 / 10.0F;

			return drawCalls + " draw calls / " + renderTypes + " render types = "
				+ effectiveness + "% effective";
		} else {
			return "(no draw calls)";
		}
	}
}
