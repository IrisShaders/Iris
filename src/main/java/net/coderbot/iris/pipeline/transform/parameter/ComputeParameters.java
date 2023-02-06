package net.coderbot.iris.pipeline.transform.parameter;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import net.coderbot.iris.gl.blending.AlphaTest;
import net.coderbot.iris.gl.texture.TextureType;
import net.coderbot.iris.helpers.Tri;
import net.coderbot.iris.pipeline.transform.Patch;
import net.coderbot.iris.shaderpack.texture.TextureStage;

public class ComputeParameters extends TextureStageParameters {
	public ComputeParameters(Patch patch, TextureStage stage,
			Object2ObjectMap<Tri<String, TextureType, TextureStage>, String> textureMap) {
		super(patch, stage, textureMap);
	}

	@Override
	public AlphaTest getAlphaTest() {
		return AlphaTest.ALWAYS;
	}

	// since this class has no fields, hashCode() and equals() are inherited from
	// TextureStageParameters
}
