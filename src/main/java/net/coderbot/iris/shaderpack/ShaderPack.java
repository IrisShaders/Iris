package net.coderbot.iris.shaderpack;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;

import net.coderbot.iris.Iris;
import org.jetbrains.annotations.Nullable;

public class ShaderPack {
	private final ProgramSet base;
	@Nullable
	private final ProgramSet overworld;
	private final ProgramSet nether;
	private final ProgramSet end;

	private final IdMap idMap;
	private final LanguageMap languageMap;
	private final CustomTexture customNoiseTexture;

	public ShaderPack(Path root) throws IOException {
		// A null path is not allowed.
		Objects.requireNonNull(root);

		ShaderProperties shaderProperties = loadProperties(root, "shaders.properties")
			.map(ShaderProperties::new)
			.orElseGet(ShaderProperties::empty);

		this.base = new ProgramSet(root, root, shaderProperties, this);
		this.overworld = loadOverrides(root, "world0", shaderProperties, this);
		this.nether = loadOverrides(root, "world-1", shaderProperties, this);
		this.end = loadOverrides(root, "world1", shaderProperties, this);

		this.idMap = new IdMap(root);
		this.languageMap = new LanguageMap(root.resolve("lang"));

		customNoiseTexture = shaderProperties.getNoiseTexturePath().map(path -> {
			try {
				// TODO: Make sure the resulting path is within the shaderpack?
				byte[] content = Files.readAllBytes(root.resolve(path));

				// TODO: Read the blur / clamp data from the shaderpack...
				return new CustomTexture(content, true, false);
			} catch (IOException e) {
				Iris.logger.error("Unable to read the custom noise texture at " + path);

				return null;
			}
		}).orElse(null);
	}

	@Nullable
	private static ProgramSet loadOverrides(Path root, String subfolder, ShaderProperties shaderProperties, ShaderPack pack) throws IOException {
		Path sub = root.resolve(subfolder);

		if (Files.exists(sub)) {
			return new ProgramSet(sub, root, shaderProperties, pack);
		}

		return null;
	}

	// TODO: Copy-paste from IdMap, find a way to deduplicate this
	private static Optional<Properties> loadProperties(Path shaderPath, String name) {
		Properties properties = new Properties();

		try {
			// NB: shaders.properties is specified to be encoded with ISO-8859-1 by OptiFine,
			//     so we don't need to do the UTF-8 workaround here.
			properties.load(Files.newInputStream(shaderPath.resolve(name)));
		} catch (IOException e) {
			Iris.logger.debug("An " + name + " file was not found in the current shaderpack");

			return Optional.empty();
		}

		return Optional.of(properties);
	}

	public ProgramSet getProgramSet(DimensionId dimension) {
		ProgramSet overrides;

		switch (dimension) {
			case OVERWORLD:
				overrides = overworld;
				break;
			case NETHER:
				overrides = nether;
				break;
			case END:
				overrides = end;
				break;
			default:
				throw new IllegalArgumentException("Unknown dimension " + dimension);
		}

		return ProgramSet.merged(base, overrides);
	}

	public IdMap getIdMap() {
		return idMap;
	}

	public Optional<CustomTexture> getCustomNoiseTexture() {
		return Optional.ofNullable(customNoiseTexture);
	}

	public LanguageMap getLanguageMap() {
		return languageMap;
	}
}
