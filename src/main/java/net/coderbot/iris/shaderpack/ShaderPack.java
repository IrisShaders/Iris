package net.coderbot.iris.shaderpack;

import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Function;

import com.google.common.collect.ImmutableList;
import net.coderbot.iris.Iris;
import net.coderbot.iris.gl.program.ProgramBuilder;
import net.coderbot.iris.gl.shader.GlShader;
import net.coderbot.iris.gl.shader.ShaderConstants;
import net.coderbot.iris.shaderpack.include.AbsolutePackPath;
import net.coderbot.iris.shaderpack.include.IncludeGraph;
import net.coderbot.iris.shaderpack.include.IncludeProcessor;
import net.coderbot.iris.shaderpack.include.ShaderPackSourceNames;
import net.coderbot.iris.shaderpack.transform.line.LineTransform;
import net.coderbot.iris.shaderpack.transform.line.VersionDirectiveNormalizer;
import org.apache.logging.log4j.Level;
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

	/**
	 * Reads a shader pack from the disk.
	 *
	 * @param root The path to the "shaders" directory within the shader pack
	 * @throws IOException
	 */
	public ShaderPack(Path root) throws IOException {
		// A null path is not allowed.
		Objects.requireNonNull(root);

		ShaderProperties shaderProperties = loadProperties(root, "shaders.properties")
			.map(ShaderProperties::new)
			.orElseGet(ShaderProperties::empty);

		ImmutableList.Builder<AbsolutePackPath> starts = ImmutableList.builder();
		ImmutableList<String> potentialFileNames = ShaderPackSourceNames.POTENTIAL_STARTS;

		ShaderPackSourceNames.findPresentSources(starts, root, AbsolutePackPath.fromAbsolutePath("/"),
				potentialFileNames);

		boolean hasWorld0 = ShaderPackSourceNames.findPresentSources(starts, root,
				AbsolutePackPath.fromAbsolutePath("/world0"), potentialFileNames);

		boolean hasNether = ShaderPackSourceNames.findPresentSources(starts, root,
				AbsolutePackPath.fromAbsolutePath("/world-1"), potentialFileNames);

		boolean hasEnd = ShaderPackSourceNames.findPresentSources(starts, root,
				AbsolutePackPath.fromAbsolutePath("/world1"), potentialFileNames);

		IncludeGraph graph = new IncludeGraph(root, starts.build());

		// TODO: Discover shader options
		// TODO: Merge shader options
		// TODO: Apply shader options

		IncludeProcessor includeProcessor = new IncludeProcessor(graph);

		Function<AbsolutePackPath, String> sourceProvider = (path) -> {
			ImmutableList<String> lines = includeProcessor.getIncludedFile(path);

			if (lines == null) {
				return null;
			}

			// Normalize version directives.
			lines = LineTransform.apply(lines, VersionDirectiveNormalizer.INSTANCE);

			StringBuilder builder = new StringBuilder();

			for (String line : lines) {
				builder.append(line);
				builder.append('\n');
			}

			// Apply shader environment defines / constants
			// TODO: Write our own code pathways for this
			ShaderConstants constants = ProgramBuilder.MACRO_CONSTANTS;
			String source = GlShader.processShader(builder.toString(), constants);

			// TODO: Apply GLSL preprocessor to source

			return source;
		};

		this.base = new ProgramSet(AbsolutePackPath.fromAbsolutePath("/"), sourceProvider, shaderProperties, this);

		this.overworld = loadOverrides(hasWorld0, AbsolutePackPath.fromAbsolutePath("/world0"), sourceProvider,
				shaderProperties, this);
		this.nether = loadOverrides(hasNether, AbsolutePackPath.fromAbsolutePath("/world-1"), sourceProvider,
				shaderProperties, this);
		this.end = loadOverrides(hasEnd, AbsolutePackPath.fromAbsolutePath("/world1"), sourceProvider,
				shaderProperties, this);

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
	private static ProgramSet loadOverrides(boolean has, AbsolutePackPath path, Function<AbsolutePackPath, String> sourceProvider,
											ShaderProperties shaderProperties, ShaderPack pack) {
		if (has) {
			return new ProgramSet(path, sourceProvider, shaderProperties, pack);
		}

		return null;
	}

	// TODO: Copy-paste from IdMap, find a way to deduplicate this
	private static Optional<Properties> loadProperties(Path shaderPath, String name) {
		String fileContents = readProperties(shaderPath, name);
		if (fileContents == null) {
			return Optional.empty();
		}

		String processed = PropertiesPreprocessor.process(shaderPath.getParent(), shaderPath, fileContents);

		StringReader propertiesReader = new StringReader(processed);
		Properties properties = new Properties();
		try {
			// NB: ID maps are specified to be encoded with ISO-8859-1 by OptiFine,
			//     so we don't need to do the UTF-8 workaround here.
			properties.load(propertiesReader);
		} catch (IOException e) {
			Iris.logger.error("Error loading " + name + " at " + shaderPath);
			Iris.logger.catching(Level.ERROR, e);

			return Optional.empty();
		}

		return Optional.of(properties);
	}

	private static String readProperties(Path shaderPath, String name) {
		try {
			return new String(Files.readAllBytes(shaderPath.resolve(name)), StandardCharsets.UTF_8);
		} catch (NoSuchFileException e) {
			Iris.logger.debug("An " + name + " file was not found in the current shaderpack");

			return null;
		} catch (IOException e) {
			Iris.logger.error("An IOException occurred reading " + name + " from the current shaderpack");
			Iris.logger.catching(Level.ERROR, e);

			return null;
		}
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

		// NB: If a dimension overrides directory is present, none of the files from the parent directory are "merged"
		//     into the override. Rather, we act as if the overrides directory contains a completely different set of
		//     shader programs unrelated to that of the base shader pack.
		//
		//     This makes sense because if base defined a composite pass and the override didn't, it would make it
		//     impossible to "un-define" the composite pass. It also removes a lot of complexity related to "merging"
		//     program sets. At the same time, this might be desired behavior by shader pack authors. It could make
		//     sense to bring it back as a configurable option, and have a more maintainable set of code backing it.
		if (overrides != null) {
			return overrides;
		} else {
			return base;
		}
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
