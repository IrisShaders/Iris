package net.coderbot.iris.pipeline.transform;

import net.coderbot.iris.gl.blending.AlphaTest;
import net.coderbot.iris.gl.shader.ShaderType;
import net.coderbot.iris.pipeline.newshader.ShaderAttributeInputs;

public class VanillaParameters extends OverlayParameters {
	public final AlphaTest alpha;
	public final boolean hasChunkOffset;
	public final ShaderAttributeInputs inputs;

	public VanillaParameters(Patch patch, ShaderType type, AlphaTest alpha, boolean hasChunkOffset,
			ShaderAttributeInputs inputs, boolean hasGeometry) {
		super(patch, type, hasGeometry);
		this.alpha = alpha;
		this.hasChunkOffset = hasChunkOffset;
		this.inputs = inputs;
	}
}
