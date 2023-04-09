package net.irisshaders.iris.pipeline.transform.parameter;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import net.irisshaders.iris.gbuffer_overrides.matching.InputAvailability;
import net.irisshaders.iris.gl.texture.TextureType;
import net.irisshaders.iris.helpers.Tri;
import net.irisshaders.iris.pipeline.transform.Patch;
import net.irisshaders.iris.shaderpack.texture.TextureStage;

public class AttributeParameters extends GeometryInfoParameters {
	public final InputAvailability inputs;
	// WARNING: adding new fields requires updating hashCode and equals methods!

	public AttributeParameters(
			Patch patch,
			Object2ObjectMap<Tri<String, TextureType, TextureStage>, String> textureMap,
			boolean hasGeometry,
			InputAvailability inputs) {
		super(patch, textureMap, hasGeometry);
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
			return other.inputs == null;
		} else
			return inputs.equals(other.inputs);
	}
}
