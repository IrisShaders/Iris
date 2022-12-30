package net.coderbot.iris.pipeline.transform.parameter;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import net.coderbot.iris.gbuffer_overrides.matching.InputAvailability;
import net.coderbot.iris.gl.texture.TextureType;
import net.coderbot.iris.helpers.Tri;
import net.coderbot.iris.pipeline.transform.Patch;
import net.coderbot.iris.shaderpack.texture.TextureStage;

public class AttributeParameters extends OverlayParameters {
	public final InputAvailability inputs;
	private final Object2ObjectMap<Tri<String, TextureType, TextureStage>, String> textureMap;

	public AttributeParameters(Patch patch, boolean hasGeometry, InputAvailability inputs, Object2ObjectMap<Tri<String, TextureType, TextureStage>, String> textureMap) {
		super(patch, hasGeometry);
		this.inputs = inputs;
		this.textureMap = textureMap;
	}

	public Object2ObjectMap<Tri<String, TextureType, TextureStage>, String> getTextureMap() {
		return textureMap;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((inputs == null) ? 0 : inputs.hashCode());
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
		AttributeParameters other = (AttributeParameters) obj;
		if (inputs == null) {
			if (other.inputs != null)
				return false;
		} else if (!inputs.equals(other.inputs))
			return false;
		if (textureMap == null) {
			if (other.textureMap != null)
				return false;
		} else if (!textureMap.equals(other.textureMap))
			return false;
		return true;
	}
}
