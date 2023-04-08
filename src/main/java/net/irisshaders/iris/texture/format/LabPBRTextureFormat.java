package net.irisshaders.iris.texture.format;

import net.irisshaders.iris.texture.mipmap.ChannelMipmapGenerator;
import net.irisshaders.iris.texture.mipmap.CustomMipmapGenerator;
import net.irisshaders.iris.texture.mipmap.DiscreteBlendFunction;
import net.irisshaders.iris.texture.mipmap.LinearBlendFunction;
import net.irisshaders.iris.texture.pbr.PBRType;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class LabPBRTextureFormat implements TextureFormat {
	public static final ChannelMipmapGenerator SPECULAR_MIPMAP_GENERATOR = new ChannelMipmapGenerator(
		LinearBlendFunction.INSTANCE,
		new DiscreteBlendFunction(v -> v < 230 ? 0 : v - 229),
		new DiscreteBlendFunction(v -> v < 65 ? 0 : 1),
		new DiscreteBlendFunction(v -> v < 255 ? 0 : 1)
	);

	private final String name;
	@Nullable
	private final String version;

	public LabPBRTextureFormat(String name, @Nullable String version) {
		this.name = name;
		this.version = version;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public @Nullable String getVersion() {
		return version;
	}

	@Override
	public boolean canInterpolateValues(PBRType pbrType) {
		return pbrType != PBRType.SPECULAR;
	}

	@Override
	public @Nullable CustomMipmapGenerator getMipmapGenerator(PBRType pbrType) {
		if (pbrType == PBRType.SPECULAR) {
			return SPECULAR_MIPMAP_GENERATOR;
		}
		return null;
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, version);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LabPBRTextureFormat other = (LabPBRTextureFormat) obj;
		return Objects.equals(name, other.name) && Objects.equals(version, other.version);
	}
}
