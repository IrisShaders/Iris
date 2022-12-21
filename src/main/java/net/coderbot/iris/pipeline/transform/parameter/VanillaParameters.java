package net.coderbot.iris.pipeline.transform.parameter;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import net.coderbot.iris.gl.blending.AlphaTest;
import net.coderbot.iris.gl.texture.TextureType;
import net.coderbot.iris.helpers.Tri;
import net.coderbot.iris.pipeline.newshader.ShaderAttributeInputs;
import net.coderbot.iris.pipeline.transform.Patch;
import net.coderbot.iris.shaderpack.texture.TextureStage;

public class VanillaParameters extends OverlayParameters {
	public final AlphaTest alpha;
	public final ShaderAttributeInputs inputs;
	public final boolean hasChunkOffset;
	private final Object2ObjectMap<Tri<String, TextureType, TextureStage>, String> textureMap;

	public VanillaParameters(Patch patch, AlphaTest alpha, boolean hasChunkOffset,
			ShaderAttributeInputs inputs, boolean hasGeometry, Object2ObjectMap<Tri<String, TextureType, TextureStage>, String> textureMap) {
		super(patch, hasGeometry);
		this.alpha = alpha;
		this.hasChunkOffset = hasChunkOffset;
		this.textureMap = textureMap;
		this.inputs = inputs;
	}

	public Object2ObjectMap<Tri<String, TextureType, TextureStage>, String> getTextureMap() {
		return textureMap;
	}

	@Override
	public AlphaTest getAlphaTest() {
		return alpha;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((alpha == null) ? 0 : alpha.hashCode());
		result = prime * result + ((inputs == null) ? 0 : inputs.hashCode());
		result = prime * result + (hasChunkOffset ? 1231 : 1237);
		result = prime * result + ((textureMap == null) ? 0 : textureMap.hashCode());
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
		if (textureMap == null) {
			if (other.textureMap != null)
				return false;
		} else if (!textureMap.equals(other.textureMap))
			return false;
		return true;
	}
}
