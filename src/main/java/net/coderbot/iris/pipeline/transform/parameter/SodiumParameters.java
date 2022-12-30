package net.coderbot.iris.pipeline.transform.parameter;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import net.coderbot.iris.gl.blending.AlphaTest;
import net.coderbot.iris.gl.texture.TextureType;
import net.coderbot.iris.helpers.Tri;
import net.coderbot.iris.pipeline.newshader.ShaderAttributeInputs;
import net.coderbot.iris.pipeline.transform.Patch;
import net.coderbot.iris.pipeline.transform.PatchShaderType;
import net.coderbot.iris.shaderpack.texture.TextureStage;

public class SodiumParameters extends Parameters {
	private final AlphaTest cutoutAlpha;
	private final AlphaTest defaultAlpha;
	public final ShaderAttributeInputs inputs;
	public final float positionScale;
	public final float positionOffset;
	public final float textureScale;
	private final Object2ObjectMap<Tri<String, TextureType, TextureStage>, String> textureMap;

	public AlphaTest alpha;

	public SodiumParameters(Patch patch, AlphaTest cutoutAlpha, AlphaTest defaultAlpha, ShaderAttributeInputs inputs,
			float positionScale, float positionOffset, float textureScale, Object2ObjectMap<Tri<String, TextureType, TextureStage>, String> textureMap) {
		super(patch);
		this.cutoutAlpha = cutoutAlpha;
		this.defaultAlpha = defaultAlpha;
		this.inputs = inputs;
		this.positionScale = positionScale;
		this.positionOffset = positionOffset;
		this.textureScale = textureScale;
		this.textureMap = textureMap;

		this.alpha = defaultAlpha;
	}

	public Object2ObjectMap<Tri<String, TextureType, TextureStage>, String> getTextureMap() {
		return textureMap;
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
		result = prime * result + ((cutoutAlpha == null) ? 0 : cutoutAlpha.hashCode());
		result = prime * result + ((defaultAlpha == null) ? 0 : defaultAlpha.hashCode());
		result = prime * result + ((inputs == null) ? 0 : inputs.hashCode());
		result = prime * result + Float.floatToIntBits(positionScale);
		result = prime * result + Float.floatToIntBits(positionOffset);
		result = prime * result + Float.floatToIntBits(textureScale);
		result = prime * result + ((textureMap == null) ? 0 : textureMap.hashCode());
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
		if (cutoutAlpha == null) {
			if (other.cutoutAlpha != null)
				return false;
		} else if (!cutoutAlpha.equals(other.cutoutAlpha))
			return false;
		if (defaultAlpha == null) {
			if (other.defaultAlpha != null)
				return false;
		} else if (!defaultAlpha.equals(other.defaultAlpha))
			return false;
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
		if (textureMap == null) {
			if (other.textureMap != null)
				return false;
		} else if (!textureMap.equals(other.textureMap))
			return false;
		if (alpha == null) {
			if (other.alpha != null)
				return false;
		} else if (!alpha.equals(other.alpha))
			return false;
		return true;
	}
}
