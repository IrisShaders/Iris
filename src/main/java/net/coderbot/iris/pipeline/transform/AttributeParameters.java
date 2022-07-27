package net.coderbot.iris.pipeline.transform;

import net.coderbot.iris.gbuffer_overrides.matching.InputAvailability;
import net.coderbot.iris.gl.shader.ShaderType;

class AttributeParameters extends OverlayParameters {
	public final InputAvailability inputs;

	public AttributeParameters(Patch patch, ShaderType type, boolean hasGeometry, InputAvailability inputs) {
		super(patch, type, hasGeometry);
		this.inputs = inputs;
	}
}
