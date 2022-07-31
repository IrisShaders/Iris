package net.coderbot.iris.gbuffer_overrides.matching;

public class InputAvailability {
	public static final int NUM_VALUES = 8;

	public final boolean texture;
	public final boolean lightmap;
	public final boolean overlay;

	public InputAvailability(boolean texture, boolean lightmap, boolean overlay) {
		this.texture = texture;
		this.lightmap = lightmap;
		this.overlay = overlay;
	}

	public InputAvailability withoutOverlay() {
		return new InputAvailability(texture, lightmap, false);
	}

	public static InputAvailability unpack(int packed) {
		return new InputAvailability((packed & 1) == 1, (packed & 2) == 2, (packed & 4) == 4);
	}

	public int pack() {
		int packed = 0;

		if (overlay) {
			packed |= 4;
		}

		if (lightmap) {
			packed |= 2;
		}

		if (texture) {
			packed |= 1;
		}

		return packed;
	}

	@Override
	public String toString() {
		return "InputAvailability{" +
			"texture=" + texture +
			", lightmap=" + lightmap +
			", overlay=" + overlay +
			'}';
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (lightmap ? 1231 : 1237);
		result = prime * result + (overlay ? 1231 : 1237);
		result = prime * result + (texture ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		InputAvailability other = (InputAvailability) obj;
		if (lightmap != other.lightmap)
			return false;
		if (overlay != other.overlay)
			return false;
		if (texture != other.texture)
			return false;
		return true;
	}
}
