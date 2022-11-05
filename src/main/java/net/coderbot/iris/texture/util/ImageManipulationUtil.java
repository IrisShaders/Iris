package net.coderbot.iris.texture.util;

import com.mojang.blaze3d.platform.NativeImage;

public class ImageManipulationUtil {
	public static NativeImage scaleNearestNeighbor(NativeImage image, int newWidth, int newHeight) {
		NativeImage scaled = new NativeImage(image.format(), newWidth, newHeight, false);
		float xScale = (float) newWidth / image.getWidth();
		float yScale = (float) newHeight / image.getHeight();
		for (int y = 0; y < newHeight; ++y) {
			for (int x = 0; x < newWidth; ++x) {
				float unscaledX = (x + 0.5f) / xScale;
				float unscaledY = (y + 0.5f) / yScale;
				scaled.setPixelRGBA(x, y, image.getPixelRGBA((int) unscaledX, (int) unscaledY));
			}
		}
		return scaled;
	}

	public static NativeImage scaleBilinear(NativeImage image, int newWidth, int newHeight) {
		NativeImage scaled = new NativeImage(image.format(), newWidth, newHeight, false);
		float xScale = (float) newWidth / image.getWidth();
		float yScale = (float) newHeight / image.getHeight();
		for (int y = 0; y < newHeight; ++y) {
			for (int x = 0; x < newWidth; ++x) {
				float unscaledX = (x + 0.5f) / xScale;
				float unscaledY = (y + 0.5f) / yScale;

				int x1 = Math.round(unscaledX);
				int y1 = Math.round(unscaledY);
				int x0 = x1 - 1;
				int y0 = y1 - 1;

				boolean x0valid = true;
				boolean y0valid = true;
				boolean x1valid = true;
				boolean y1valid = true;
				if (x0 < 0) {
					x0valid = false;
				}
				if (y0 < 0) {
					y0valid = false;
				}
				if (x1 >= image.getWidth()) {
					x1valid = false;
				}
				if (y1 >= image.getHeight()) {
					y1valid = false;
				}

				int finalColor = 0;
				if (x0valid & y0valid & x1valid & y1valid) {
					float leftWeight = (x1 + 0.5f) - unscaledX;
					float rightWeight = unscaledX - (x0 + 0.5f);
					float topWeight = (y1 + 0.5f) - unscaledY;
					float bottomWeight = unscaledY - (y0 + 0.5f);

					float weightTL = leftWeight * topWeight;
					float weightTR = rightWeight * topWeight;
					float weightBL = leftWeight * bottomWeight;
					float weightBR = rightWeight * bottomWeight;

					int colorTL = image.getPixelRGBA(x0, y0);
					int colorTR = image.getPixelRGBA(x1, y0);
					int colorBL = image.getPixelRGBA(x0, y1);
					int colorBR = image.getPixelRGBA(x1, y1);

					finalColor = blendColor(colorTL, colorTR, colorBL, colorBR, weightTL, weightTR, weightBL, weightBR);
				} else if (x0valid & x1valid) {
					float leftWeight = (x1 + 0.5f) - unscaledX;
					float rightWeight = unscaledX - (x0 + 0.5f);

					int validY = y0valid ? y0 : y1;
					int colorLeft = image.getPixelRGBA(x0, validY);
					int colorRight = image.getPixelRGBA(x1, validY);

					finalColor = blendColor(colorLeft, colorRight, leftWeight, rightWeight);
				} else if (y0valid & y1valid) {
					float topWeight = (y1 + 0.5f) - unscaledY;
					float bottomWeight = unscaledY - (y0 + 0.5f);

					int validX = x0valid ? x0 : x1;
					int colorTop = image.getPixelRGBA(validX, y0);
					int colorBottom = image.getPixelRGBA(validX, y1);

					finalColor = blendColor(colorTop, colorBottom, topWeight, bottomWeight);
				} else {
					finalColor = image.getPixelRGBA(x0valid ? x0 : x1, y0valid ? y0 : y1);
				}
				scaled.setPixelRGBA(x, y, finalColor);
			}
		}
		return scaled;
	}

	private static int blendColor(int c0, int c1, int c2, int c3, float w0, float w1, float w2, float w3) {
		return NativeImage.combine(
				blendChannel(NativeImage.getA(c0), NativeImage.getA(c1), NativeImage.getA(c2), NativeImage.getA(c3), w0, w1, w2, w3),
				blendChannel(NativeImage.getB(c0), NativeImage.getB(c1), NativeImage.getB(c2), NativeImage.getB(c3), w0, w1, w2, w3),
				blendChannel(NativeImage.getG(c0), NativeImage.getG(c1), NativeImage.getG(c2), NativeImage.getG(c3), w0, w1, w2, w3),
				blendChannel(NativeImage.getR(c0), NativeImage.getR(c1), NativeImage.getR(c2), NativeImage.getR(c3), w0, w1, w2, w3)
		);
	}

	private static int blendChannel(int v0, int v1, int v2, int v3, float w0, float w1, float w2, float w3) {
		return Math.round(v0 * w0 + v1 * w1 + v2 * w2 + v3 * w3);
	}

	private static int blendColor(int c0, int c1, float w0, float w1) {
		return NativeImage.combine(
				blendChannel(NativeImage.getA(c0), NativeImage.getA(c1), w0, w1),
				blendChannel(NativeImage.getB(c0), NativeImage.getB(c1), w0, w1),
				blendChannel(NativeImage.getG(c0), NativeImage.getG(c1), w0, w1),
				blendChannel(NativeImage.getR(c0), NativeImage.getR(c1), w0, w1)
		);
	}

	private static int blendChannel(int v0, int v1, float w0, float w1) {
		return Math.round(v0 * w0 + v1 * w1);
	}
}
