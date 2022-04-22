package net.coderbot.iris.texture.format;

import net.coderbot.iris.texture.mipmap.CustomMipmapGenerator;
import net.coderbot.iris.texture.pbr.PBRType;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.function.Consumer;

public interface TextureFormat {
	String getName();

	@Nullable
	String getVersion();

	// TODO: call this method when adding directives
	default void addDirectives(Consumer<String> consumer) {
		String directiveName = getName().toUpperCase(Locale.ROOT).replaceAll("-", "_");
		String directive = "MC_TEXTURE_FORMAT_" + directiveName;
		consumer.accept(directive);

		String version = getVersion();
		if (version != null) {
			String directiveVersion = version.replaceAll("\\.-", "_");
			String versionDirective = directive + "_" + directiveVersion;
			consumer.accept(versionDirective);
		}
	}

	@Nullable
	CustomMipmapGenerator getMipmapGenerator(PBRType pbrType);

	public interface Factory {
		TextureFormat createFormat(String name, @Nullable String version);
	}
}
