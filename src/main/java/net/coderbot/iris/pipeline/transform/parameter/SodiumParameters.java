package net.coderbot.iris.pipeline.transform.parameter;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import net.coderbot.iris.gl.blending.AlphaTest;
import net.coderbot.iris.gl.texture.TextureType;
import net.coderbot.iris.helpers.Tri;
import net.coderbot.iris.pipeline.newshader.ShaderAttributeInputs;
import net.coderbot.iris.pipeline.transform.Patch;
import net.coderbot.iris.shaderpack.texture.TextureStage;

public class SodiumParameters extends GeometryInfoParameters {
	public final ShaderAttributeInputs inputs;
	public final float positionScale;
	public final float positionOffset;
	public final float textureScale;
	// WARNING: adding new fields requires updating hashCode and equals methods!

	// DO NOT include this field in hashCode or equals, it's mutable!
	// (See use of setAlphaFor in TransformPatcher)
	public AlphaTest alpha;

	public SodiumParameters(Patch patch,
			Object2ObjectMap<Tri<String, TextureType, TextureStage>, String> textureMap,
			AlphaTest alpha,
			ShaderAttributeInputs inputs,
			float positionScale, float positionOffset, float textureScale, boolean hasGeometry) {
		super(patch, textureMap, hasGeometry);
		this.inputs = inputs;
		this.positionScale = positionScale;
		this.positionOffset = positionOffset;
		this.textureScale = textureScale;

		this.alpha = alpha;
	}
	@Override
	public AlphaTest getAlphaTest() {
		return alpha;
	}

	@Override
	public TextureStage getTextureStage() {
		return TextureStage.GBUFFERS_AND_SHADOW;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((inputs == null) ? 0 : inputs.hashCode());
		result = prime * result + Float.floatToIntBits(positionScale);
		result = prime * result + Float.floatToIntBits(positionOffset);
		result = prime * result + Float.floatToIntBits(textureScale);
		result = prime * result + ((alpha == null) ? 0 : alpha.hashCode());
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
		if (inputs == null) {
			if (other.inputs != null)
				return false;
		} else if (!inputs.equals(other.inputs))
			return false;
		if (Float.floatToIntBits(positionScale) != Float.floatToIntBits(other.positionScale))
			return false;
		if (Float.floatToIntBits(positionOffset) != Float.floatToIntBits(other.positionOffset))
			return false;
		if (Float.floatToIntBits(textureScale) != Float.floatToIntBits(other.textureScale))
			return false;
		if (alpha == null) {
			if (other.alpha != null)
				return false;
		} else if (!alpha.equals(other.alpha))
			return false;
		return true;
	}
}
