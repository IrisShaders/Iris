package net.coderbot.iris.texunits;

import org.lwjgl.opengl.GL15;

public enum TextureUnit {
	TERRAIN(0),
	LIGHTMAP(1),
	// TODO: Relocate this to a different texture unit, this is used by the normal map for normal shaders
	OVERLAY(2);

	private final int samplerId;
	private final int unitId;

	TextureUnit(int samplerId) {
		this.samplerId = samplerId;
		this.unitId = GL15.GL_TEXTURE0 + samplerId;
	}

	public int getSamplerId() {
		return samplerId;
	}

	public int getUnitId() {
		return unitId;
	}

	static {
		if (TERRAIN.getSamplerId() != 0) {
			throw new IllegalStateException("The texture unit number of TERRAIN is not configurable.");
		}
	}
}
