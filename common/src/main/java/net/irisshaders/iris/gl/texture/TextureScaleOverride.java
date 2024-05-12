package net.irisshaders.iris.gl.texture;

public class TextureScaleOverride {
	public final boolean isXRelative, isYRelative;
	public float relativeX, relativeY;
	public int sizeX, sizeY;

	public TextureScaleOverride(String xValue, String yValue) {
		if (xValue.contains(".")) {
			this.relativeX = Float.parseFloat(xValue);
			this.isXRelative = true;
		} else {
			this.sizeX = Integer.parseInt(xValue);
			this.isXRelative = false;
		}

		if (yValue.contains(".")) {
			this.relativeY = Float.parseFloat(yValue);
			this.isYRelative = true;
		} else {
			this.sizeY = Integer.parseInt(yValue);
			this.isYRelative = false;
		}
	}

	public int getX(int originalX) {
		if (isXRelative) {
			return (int) (originalX * relativeX);
		} else {
			return sizeX;
		}
	}

	public int getY(int originalY) {
		if (isYRelative) {
			return (int) (originalY * relativeY);
		} else {
			return sizeY;
		}
	}
}
