package net.coderbot.iris.pipeline.transform;

import net.coderbot.iris.gbuffer_overrides.matching.InputAvailability;
import net.coderbot.iris.pipeline.newshader.UniformBufferObject;

class AttributeParameters extends OverlayParameters {
	public final InputAvailability inputs;

	public AttributeParameters(Patch patch, boolean hasGeometry, InputAvailability inputs, UniformBufferObject bufferObject) {
		super(patch, hasGeometry, bufferObject);
		this.inputs = inputs;
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
