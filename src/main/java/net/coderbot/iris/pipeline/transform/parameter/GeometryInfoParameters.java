package net.coderbot.iris.pipeline.transform.parameter;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import net.coderbot.iris.gl.texture.TextureType;
import net.coderbot.iris.helpers.Tri;
import net.coderbot.iris.pipeline.transform.Patch;
import net.coderbot.iris.shaderpack.texture.TextureStage;

public abstract class GeometryInfoParameters extends Parameters {
	public final boolean hasGeometry;
	// WARNING: adding new fields requires updating hashCode and equals methods!

	public GeometryInfoParameters(Patch patch,
			Object2ObjectMap<Tri<String, TextureType, TextureStage>, String> textureMap, boolean hasGeometry) {
		super(patch, textureMap);
		this.hasGeometry = hasGeometry;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (hasGeometry ? 1231 : 1237);
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
		GeometryInfoParameters other = (GeometryInfoParameters) obj;
		if (hasGeometry != other.hasGeometry)
			return false;
		return true;
	}
}
