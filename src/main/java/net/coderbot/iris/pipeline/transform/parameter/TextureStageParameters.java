package net.coderbot.iris.pipeline.transform.parameter;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import net.coderbot.iris.gl.blending.AlphaTest;
import net.coderbot.iris.gl.texture.TextureType;
import net.coderbot.iris.helpers.Tri;
import net.coderbot.iris.pipeline.transform.Patch;
import net.coderbot.iris.shaderpack.texture.TextureStage;

public class TextureStageParameters extends Parameters {
	private final TextureStage stage;

	public TextureStageParameters(Patch patch, TextureStage stage,
			Object2ObjectMap<Tri<String, TextureType, TextureStage>, String> textureMap) {
		super(patch, textureMap);
		this.stage = stage;
	}

	@Override
	public TextureStage getTextureStage() {
		return stage;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((stage == null) ? 0 : stage.hashCode());
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
		TextureStageParameters other = (TextureStageParameters) obj;
		if (stage != other.stage)
			return false;
		return true;
	}

	@Override
	public AlphaTest getAlphaTest() {
		return AlphaTest.ALWAYS;
	}
}
