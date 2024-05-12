package net.irisshaders.iris.pipeline.transform.parameter;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import net.irisshaders.iris.gl.blending.AlphaTest;
import net.irisshaders.iris.gl.state.ShaderAttributeInputs;
import net.irisshaders.iris.gl.texture.TextureType;
import net.irisshaders.iris.helpers.Tri;
import net.irisshaders.iris.pipeline.transform.Patch;
import net.irisshaders.iris.shaderpack.texture.TextureStage;

public class VanillaParameters extends GeometryInfoParameters {
	public final AlphaTest alpha;
	public final ShaderAttributeInputs inputs;
	public final boolean hasChunkOffset;
	private final boolean isLines;
	// WARNING: adding new fields requires updating hashCode and equals methods!

	public VanillaParameters(
		Patch patch,
		Object2ObjectMap<Tri<String, TextureType, TextureStage>, String> textureMap,
		AlphaTest alpha, boolean isLines, boolean hasChunkOffset,
		ShaderAttributeInputs inputs, boolean hasGeometry, boolean hasTesselation) {
		super(patch, textureMap, hasGeometry, hasTesselation);
		this.alpha = alpha;
		this.isLines = isLines;
		this.hasChunkOffset = hasChunkOffset;
		this.inputs = inputs;
	}

	public boolean isLines() {
		return isLines;
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
		result = prime * result + ((alpha == null) ? 0 : alpha.hashCode());
		result = prime * result + ((inputs == null) ? 0 : inputs.hashCode());
		result = prime * result + (hasChunkOffset ? 1231 : 1237);
		result = prime * result + (isLines ? 1231 : 1237);
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
		VanillaParameters other = (VanillaParameters) obj;
		if (alpha == null) {
			if (other.alpha != null)
				return false;
		} else if (!alpha.equals(other.alpha))
			return false;
		if (inputs == null) {
			if (other.inputs != null)
				return false;
		} else if (!inputs.equals(other.inputs))
			return false;
		if (hasChunkOffset != other.hasChunkOffset)
			return false;
		return isLines == other.isLines;
	}
}
