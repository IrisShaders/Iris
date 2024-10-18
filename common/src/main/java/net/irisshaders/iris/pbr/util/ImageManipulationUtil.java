package net.irisshaders.iris.pbr.util;

import com.mojang.blaze3d.platform.NativeImage;
import net.caffeinemc.mods.sodium.api.util.ColorABGR;

public class ImageManipulationUtil {
	public static NativeImage scaleNearestNeighbor(NativeImage image, int newWidth, int newHeight) {
		NativeImage scaled = new NativeImage(image.format(), newWidth, newHeight, false);
		float xScale = (float) newWidth / image.getWidth();
		float yScale = (float) newHeight / image.getHeight();
		for (int y = 0; y < newHeight; ++y) {
			for (int x = 0; x < newWidth; ++x) {
				float unscaledX = (x + 0.5f) / xScale;
				float unscaledY = (y + 0.5f) / yScale;
				scaled.setPixel(x, y, image.getPixel((int) unscaledX, (int) unscaledY));
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

					int colorTL = image.getPixel(x0, y0);
					int colorTR = image.getPixel(x1, y0);
					int colorBL = image.getPixel(x0, y1);
					int colorBR = image.getPixel(x1, y1);

					finalColor = blendColor(colorTL, colorTR, colorBL, colorBR, weightTL, weightTR, weightBL, weightBR);
				} else if (x0valid & x1valid) {
					float leftWeight = (x1 + 0.5f) - unscaledX;
					float rightWeight = unscaledX - (x0 + 0.5f);

					int validY = y0valid ? y0 : y1;
					int colorLeft = image.getPixel(x0, validY);
					int colorRight = image.getPixel(x1, validY);

					finalColor = blendColor(colorLeft, colorRight, leftWeight, rightWeight);
				} else if (y0valid & y1valid) {
					float topWeight = (y1 + 0.5f) - unscaledY;
					float bottomWeight = unscaledY - (y0 + 0.5f);

					int validX = x0valid ? x0 : x1;
					int colorTop = image.getPixel(validX, y0);
					int colorBottom = image.getPixel(validX, y1);

					finalColor = blendColor(colorTop, colorBottom, topWeight, bottomWeight);
				} else {
					finalColor = image.getPixel(x0valid ? x0 : x1, y0valid ? y0 : y1);
				}
				scaled.setPixel(x, y, finalColor);
			}
		}
		return scaled;
	}

	private static int packABGR(int a, int b, int g, int r) {
		return ColorABGR.pack(r,g,b,a);
	}

	private static int blendColor(int c0, int c1, int c2, int c3, float w0, float w1, float w2, float w3) {
		return packABGR(
			blendChannel(ColorABGR.unpackAlpha(c0), ColorABGR.unpackAlpha(c1), ColorABGR.unpackAlpha(c2), ColorABGR.unpackAlpha(c3), w0, w1, w2, w3),
			blendChannel(ColorABGR.unpackBlue(c0), ColorABGR.unpackBlue(c1), ColorABGR.unpackBlue(c2), ColorABGR.unpackBlue(c3), w0, w1, w2, w3),
			blendChannel(ColorABGR.unpackGreen(c0), ColorABGR.unpackGreen(c1), ColorABGR.unpackGreen(c2), ColorABGR.unpackGreen(c3), w0, w1, w2, w3),
			blendChannel(ColorABGR.unpackRed(c0), ColorABGR.unpackRed(c1), ColorABGR.unpackRed(c2), ColorABGR.unpackRed(c3), w0, w1, w2, w3)
		);
	}

	private static int blendChannel(int v0, int v1, int v2, int v3, float w0, float w1, float w2, float w3) {
		return Math.round(v0 * w0 + v1 * w1 + v2 * w2 + v3 * w3);
	}

	private static int blendColor(int c0, int c1, float w0, float w1) {
		return packABGR(
			blendChannel(ColorABGR.unpackAlpha(c0), ColorABGR.unpackAlpha(c1), w0, w1),
			blendChannel(ColorABGR.unpackBlue(c0), ColorABGR.unpackBlue(c1), w0, w1),
			blendChannel(ColorABGR.unpackGreen(c0), ColorABGR.unpackGreen(c1), w0, w1),
			blendChannel(ColorABGR.unpackRed(c0), ColorABGR.unpackRed(c1), w0, w1)
		);
	}

	private static int blendChannel(int v0, int v1, float w0, float w1) {
		return Math.round(v0 * w0 + v1 * w1);
	}
}
