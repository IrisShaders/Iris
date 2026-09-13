package net.irisshaders.iris.pipeline.transform.parameter;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import net.irisshaders.iris.gl.texture.TextureType;
import net.irisshaders.iris.helpers.Tri;
import net.irisshaders.iris.pipeline.transform.Patch;
import net.irisshaders.iris.shaderpack.texture.TextureStage;

import java.util.Set;

public class DHParameters extends Parameters {
	public DHParameters(Patch patch, Object2ObjectMap<Tri<String, TextureType, TextureStage>, String> textureMap, Set<String> textureOverrides) {
		super(patch, textureMap, textureOverrides);
	}

	@Override
	public TextureStage getTextureStage() {
		return TextureStage.GBUFFERS_AND_SHADOW;
	}
}
