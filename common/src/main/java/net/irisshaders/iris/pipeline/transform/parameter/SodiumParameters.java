package net.irisshaders.iris.pipeline.transform.parameter;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import net.caffeinemc.mods.sodium.client.render.chunk.vertex.format.ChunkVertexType;
import net.irisshaders.iris.gl.blending.AlphaTest;
import net.irisshaders.iris.gl.state.ShaderAttributeInputs;
import net.irisshaders.iris.gl.texture.TextureType;
import net.irisshaders.iris.helpers.Tri;
import net.irisshaders.iris.pipeline.transform.Patch;
import net.irisshaders.iris.shaderpack.texture.TextureStage;

public class SodiumParameters extends Parameters {
	public final ChunkVertexType vertexType;
	// WARNING: adding new fields requires updating hashCode and equals methods!

	// DO NOT include this field in hashCode or equals, it's mutable!
	// (See use of setAlphaFor in TransformPatcher)
	public AlphaTest alpha;

	public SodiumParameters(Patch patch,
							Object2ObjectMap<Tri<String, TextureType, TextureStage>, String> textureMap,
							AlphaTest alpha, ChunkVertexType vertexType) {
		super(patch, textureMap);
		this.vertexType = vertexType;

		this.alpha = alpha;
	}

	@Override
	public AlphaTest getAlphaTest() {
		return alpha;
	}

	@Override
	public TextureStage getTextureStage() {
		return TextureStage.GBUFFERS_AND_SHADOW;
	}

	public ChunkVertexType getVertexType() {
		return vertexType;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((vertexType == null) ? 0 : vertexType.hashCode());
		result = prime * result + ((alpha == null) ? 0 : alpha.hashCode());
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
		SodiumParameters other = (SodiumParameters) obj;
		if (vertexType == null) {
			if (other.vertexType != null)
				return false;
		} else if (!vertexType.equals(other.vertexType))
			return false;
		if (alpha == null) {
			return other.alpha == null;
		} else return alpha.equals(other.alpha);
	}
}
