package net.coderbot.iris.pipeline.transform.parameter;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import net.coderbot.iris.gbuffer_overrides.matching.InputAvailability;
import net.coderbot.iris.gl.texture.TextureType;
import net.coderbot.iris.helpers.Tri;
import net.coderbot.iris.pipeline.transform.Patch;
import net.coderbot.iris.shaderpack.texture.TextureStage;

public class AttributeParameters extends GeometryInfoParameters {
	public final InputAvailability inputs;
	// WARNING: adding new fields requires updating hashCode and equals methods!

	public AttributeParameters(Patch patch,
			Object2ObjectMap<Tri<String, TextureType, TextureStage>, String> textureMap,
			boolean hasGeometry,
			InputAvailability inputs) {
		super(patch, textureMap, hasGeometry, false);
		this.inputs = inputs;
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
		return true;
	}
}
