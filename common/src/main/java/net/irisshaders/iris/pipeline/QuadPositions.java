package net.irisshaders.iris.pipeline;

import net.irisshaders.iris.shadows.ShadowRenderingState;

public class QuadPositions {
	public final float[] currentX = new float[4];
	public final float[] currentY = new float[4];
	public final float[] currentZ = new float[4];

	public final float[] velocityX = new float[4];
	public final float[] velocityY = new float[4];
	public final float[] velocityZ = new float[4];

	public int[] lastFrameUpdate = new int[4];

	public void setAndUpdate(int frame, int index, float x, float y, float z) {
		if (ShadowRenderingState.areShadowsCurrentlyBeingRendered() || frame == lastFrameUpdate[index]) return;

		if (frame - lastFrameUpdate[index] > 3) {
			// Reset it all.
			currentX[index] = x;
			currentY[index] = y;
			currentZ[index] = z;

			velocityX[index] = 0;
			velocityY[index] = 0;
			velocityZ[index] = 0;
		}

		this.lastFrameUpdate[index] = frame;

		velocityX[index] = x - currentX[index];
		velocityY[index] = y - currentY[index];
		velocityZ[index] = z - currentZ[index];

		currentX[index] = x;
		currentY[index] = y;
		currentZ[index] = z;
	}
}
