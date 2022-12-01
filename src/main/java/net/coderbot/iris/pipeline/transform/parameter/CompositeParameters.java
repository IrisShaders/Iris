package net.coderbot.iris.pipeline.transform.parameter;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import net.coderbot.iris.gl.blending.AlphaTest;
import net.coderbot.iris.gl.texture.TextureType;
import net.coderbot.iris.helpers.Tri;
import net.coderbot.iris.pipeline.transform.Patch;
import net.coderbot.iris.shaderpack.texture.TextureStage;

public class CompositeParameters extends Parameters {
	public final TextureStage stage;
	private final Object2ObjectMap<Tri<String, TextureType, TextureStage>, String> textureMap;

	public CompositeParameters(Patch patch, TextureStage stage, Object2ObjectMap<Tri<String, TextureType, TextureStage>, String> textureMap) {
		super(patch);
		this.stage = stage;
		this.textureMap = textureMap;
	}

	public Object2ObjectMap<Tri<String, TextureType, TextureStage>, String> getTextureMap() {
		return textureMap;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((stage == null) ? 0 : stage.hashCode());
		result = prime * result + ((textureMap == null) ? 0 : textureMap.hashCode());
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
		CompositeParameters other = (CompositeParameters) obj;
		return stage == other.stage && textureMap.equals(other.textureMap);
	}

	@Override
	public AlphaTest getAlphaTest() {
		return AlphaTest.ALWAYS;
	}
}
