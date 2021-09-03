package net.coderbot.batchedentityrendering.impl;

public class BatchingDebugMessageHelper {
	public static String getDebugMessage(DrawCallTrackingBufferBuilderStorage drawTracker) {
		int drawCalls = drawTracker.getDrawCalls();
		int renderTypes = drawTracker.getRenderTypes();

		if (drawCalls > 0) {
			int effectivenessTimes10 = renderTypes * 1000 / drawCalls;
			float effectiveness = effectivenessTimes10 / 10.0F;

			return drawCalls + " draw calls / " + renderTypes + " render types = "
					+ effectiveness + "% batching effectiveness)";
		} else {
			return "(no draw calls)";
		}
	}
}
