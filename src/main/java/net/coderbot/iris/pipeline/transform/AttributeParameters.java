package net.coderbot.iris.pipeline.transform;

import net.coderbot.iris.gbuffer_overrides.matching.InputAvailability;

class AttributeParameters extends Parameters {
	public final boolean hasGeometry;
	public final InputAvailability inputs;

	public AttributeParameters(Patch patch, boolean hasGeometry, InputAvailability inputs) {
		super(patch);
		this.hasGeometry = hasGeometry;
		this.inputs = inputs;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (hasGeometry ? 1231 : 1237);
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
		if (hasGeometry != other.hasGeometry)
			return false;
		if (inputs == null) {
			if (other.inputs != null)
				return false;
		} else if (!inputs.equals(other.inputs))
			return false;
		return true;
	}
}
