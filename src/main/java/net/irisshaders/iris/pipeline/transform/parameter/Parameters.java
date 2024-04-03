package net.irisshaders.iris.pipeline.transform.parameter;

import io.github.douira.glsl_transformer.ast.transform.JobParameters;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import net.irisshaders.iris.gl.blending.AlphaTest;
import net.irisshaders.iris.gl.texture.TextureType;
import net.irisshaders.iris.helpers.Tri;
import net.irisshaders.iris.pipeline.transform.Patch;
import net.irisshaders.iris.pipeline.transform.PatchShaderType;
import net.irisshaders.iris.shaderpack.texture.TextureStage;

public abstract class Parameters implements JobParameters {
	public final Patch patch;
	private final Object2ObjectMap<Tri<String, TextureType, TextureStage>, String> textureMap;
	public PatchShaderType type; // may only be set by TransformPatcher
	// WARNING: adding new fields requires updating hashCode and equals methods!

	public Parameters(Patch patch, Object2ObjectMap<Tri<String, TextureType, TextureStage>, String> textureMap) {
		this.patch = patch;
		this.textureMap = textureMap;
	}

	public AlphaTest getAlphaTest() {
		return AlphaTest.ALWAYS;
	}

	public abstract TextureStage getTextureStage();

	public Object2ObjectMap<Tri<String, TextureType, TextureStage>, String> getTextureMap() {
		return textureMap;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((patch == null) ? 0 : patch.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		result = prime * result + ((textureMap == null) ? 0 : textureMap.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Parameters other = (Parameters) obj;
		if (patch != other.patch)
			return false;
		if (type != other.type)
			return false;
		if (textureMap == null) {
			return other.textureMap == null;
		} else return textureMap.equals(other.textureMap);
	}
}
