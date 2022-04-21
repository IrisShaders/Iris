package net.coderbot.iris.texture.format;

import java.util.Objects;

import org.jetbrains.annotations.Nullable;

import net.coderbot.iris.texture.mipmap.ChannelMipmapGenerator;
import net.coderbot.iris.texture.mipmap.CustomMipmapGenerator;
import net.coderbot.iris.texture.mipmap.LinearBlendFunction;
import net.coderbot.iris.texture.mipmap.SpecularAlphaBlendFunction;
import net.coderbot.iris.texture.mipmap.SpecularBlueBlendFunction;
import net.coderbot.iris.texture.mipmap.SpecularGreenBlendFunction;
import net.coderbot.iris.texture.pbr.PBRType;

public class LabPBRTextureFormat implements TextureFormat {
	public static final ChannelMipmapGenerator SPECULAR_MIPMAP_GENERATOR = new ChannelMipmapGenerator(LinearBlendFunction.INSTANCE, SpecularGreenBlendFunction.INSTANCE, SpecularBlueBlendFunction.INSTANCE, SpecularAlphaBlendFunction.INSTANCE);

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
		return name == other.name && Objects.equals(version, other.version);
	}
}
