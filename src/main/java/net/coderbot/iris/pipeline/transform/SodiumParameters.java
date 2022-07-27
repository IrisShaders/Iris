package net.coderbot.iris.pipeline.transform;

import net.coderbot.iris.gl.blending.AlphaTest;
import net.coderbot.iris.gl.shader.ShaderType;
import net.coderbot.iris.pipeline.newshader.ShaderAttributeInputs;

public class SodiumParameters extends Parameters {
	public final AlphaTest alpha;
	public final ShaderAttributeInputs inputs;
	public final float positionScale;
	public final float positionOffset;
	public final float textureScale;

	public SodiumParameters(Patch patch, ShaderType type, AlphaTest alpha, ShaderAttributeInputs inputs,
			float positionScale, float positionOffset, float textureScale) {
		super(patch, type);
		this.alpha = alpha;
		this.inputs = inputs;
		this.positionScale = positionScale;
		this.positionOffset = positionOffset;
		this.textureScale = textureScale;
	}
}
