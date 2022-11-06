package net.coderbot.iris.pipeline.transform;

import net.coderbot.iris.gl.blending.AlphaTest;
import net.coderbot.iris.pipeline.newshader.ShaderAttributeInputs;
import net.coderbot.iris.pipeline.newshader.UniformBufferObject;

public class SodiumParameters extends Parameters {
	private final AlphaTest cutoutAlpha;
	private final AlphaTest defaultAlpha;
	public final ShaderAttributeInputs inputs;
	public final float positionScale;
	public final float positionOffset;
	public final float textureScale;

	public AlphaTest alpha;

	public SodiumParameters(Patch patch, AlphaTest cutoutAlpha, AlphaTest defaultAlpha, ShaderAttributeInputs inputs,
			float positionScale, float positionOffset, float textureScale, UniformBufferObject bufferObject) {
		super(patch, bufferObject);
		this.cutoutAlpha = cutoutAlpha;
		this.defaultAlpha = defaultAlpha;
		this.inputs = inputs;
		this.positionScale = positionScale;
		this.positionOffset = positionOffset;
		this.textureScale = textureScale;

		this.alpha = defaultAlpha;
	}

	public void setAlphaFor(PatchShaderType type) {
		if (type == PatchShaderType.FRAGMENT_CUTOUT) {
			alpha = cutoutAlpha;
		} else {
			alpha = defaultAlpha;
		}
	}

	public boolean hasCutoutAlpha() {
		return cutoutAlpha != null;
	}

	@Override
	public AlphaTest getAlphaTest() {
		return alpha;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((defaultAlpha == null) ? 0 : defaultAlpha.hashCode());
		result = prime * result + ((cutoutAlpha == null) ? 0 : cutoutAlpha.hashCode());
		result = prime * result + ((inputs == null) ? 0 : inputs.hashCode());
		result = prime * result + Float.floatToIntBits(positionOffset);
		result = prime * result + Float.floatToIntBits(positionScale);
		result = prime * result + Float.floatToIntBits(textureScale);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		SodiumParameters other = (SodiumParameters) obj;
		if (defaultAlpha == null) {
			if (other.defaultAlpha != null)
				return false;
		} else if (!defaultAlpha.equals(other.defaultAlpha))
			return false;
		if (cutoutAlpha == null) {
			if (other.cutoutAlpha != null)
				return false;
		} else if (!cutoutAlpha.equals(other.cutoutAlpha))
			return false;
		if (inputs == null) {
			if (other.inputs != null)
				return false;
		} else if (!inputs.equals(other.inputs))
			return false;
		if (Float.floatToIntBits(positionOffset) != Float.floatToIntBits(other.positionOffset))
			return false;
		if (Float.floatToIntBits(positionScale) != Float.floatToIntBits(other.positionScale))
			return false;
		if (Float.floatToIntBits(textureScale) != Float.floatToIntBits(other.textureScale))
			return false;
		return true;
	}
}
