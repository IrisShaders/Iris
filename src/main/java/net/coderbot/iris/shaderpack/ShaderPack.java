package net.coderbot.iris.shaderpack;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import net.coderbot.iris.Iris;
import net.coderbot.iris.gl.texture.InternalTextureFormat;
import org.apache.logging.log4j.Level;

public class ShaderPack {
	private final PackDirectives packDirectives;
	private final ProgramSource gbuffersBasic;
	private final ProgramSource gbuffersBeaconBeam;
	private final ProgramSource gbuffersTextured;
	private final ProgramSource gbuffersTexturedLit;
	private final ProgramSource gbuffersTerrain;
	private final ProgramSource gbuffersDamagedBlock;
	private final ProgramSource gbuffersWater;
	private final ProgramSource gbuffersSkyBasic;
	private final ProgramSource gbuffersSkyTextured;
	private final ProgramSource gbuffersClouds;
	private final ProgramSource gbuffersWeather;
	private final ProgramSource gbuffersEntities;
	private final ProgramSource gbuffersEntitiesGlowing;
	private final ProgramSource gbuffersGlint;
	private final ProgramSource gbuffersEntityEyes;
	private final ProgramSource gbuffersBlock;
	private final ProgramSource[] composite;
	private final ProgramSource compositeFinal;
	private final IdMap idMap;
	private final Map<String, Map<String, String>> langMap;

	public ShaderPack(Path root) throws IOException {
		ShaderProperties shaderProperties = loadProperties(root, "shaders.properties")
			.map(ShaderProperties::new)
			.orElseGet(ShaderProperties::empty);

		this.packDirectives = new PackDirectives();

		this.gbuffersBasic = readProgramSource(root, "gbuffers_basic", this, shaderProperties);
		this.gbuffersBeaconBeam = readProgramSource(root, "gbuffers_beaconbeam", this, shaderProperties);
		this.gbuffersTextured = readProgramSource(root, "gbuffers_textured", this, shaderProperties);
		this.gbuffersTexturedLit = readProgramSource(root, "gbuffers_textured_lit", this, shaderProperties);
		this.gbuffersTerrain = readProgramSource(root, "gbuffers_terrain", this, shaderProperties);
		this.gbuffersDamagedBlock = readProgramSource(root, "gbuffers_damagedblock", this, shaderProperties);
		this.gbuffersWater = readProgramSource(root, "gbuffers_water", this, shaderProperties);
		this.gbuffersSkyBasic = readProgramSource(root, "gbuffers_skybasic", this, shaderProperties);
		this.gbuffersSkyTextured = readProgramSource(root, "gbuffers_skytextured", this, shaderProperties);
		this.gbuffersClouds = readProgramSource(root, "gbuffers_clouds", this, shaderProperties);
		this.gbuffersWeather = readProgramSource(root, "gbuffers_weather", this, shaderProperties);
		this.gbuffersEntities = readProgramSource(root, "gbuffers_entities", this, shaderProperties);
		this.gbuffersEntitiesGlowing = readProgramSource(root, "gbuffers_entities_glowing", this, shaderProperties);
		this.gbuffersGlint = readProgramSource(root, "gbuffers_armor_glint", this, shaderProperties);
		this.gbuffersEntityEyes = readProgramSource(root, "gbuffers_spidereyes", this, shaderProperties);
		this.gbuffersBlock = readProgramSource(root, "gbuffers_block", this, shaderProperties);

		this.composite = new ProgramSource[16];

		for (int i = 0; i < this.composite.length; i++) {
			String suffix = i == 0 ? "" : Integer.toString(i);

			this.composite[i] = readProgramSource(root, "composite" + suffix, this, shaderProperties);
		}

		this.compositeFinal = readProgramSource(root, "final", this, shaderProperties);

		this.idMap = new IdMap(root);
		this.langMap = parseLangEntries(root);
	}

	// TODO: Copy-paste from IdMap, find a way to deduplicate this
	private static Optional<Properties> loadProperties(Path shaderPath, String name) {
		Properties properties = new Properties();

		try {
			properties.load(Files.newInputStream(shaderPath.resolve(name)));
		} catch (IOException e) {
			Iris.logger.debug("An " + name + " file was not found in the current shaderpack");

			return Optional.empty();
		}

		return Optional.of(properties);
	}

	public IdMap getIdMap() {
		return idMap;
	}

	public Optional<ProgramSource> getGbuffersBasic() {
		return gbuffersBasic.requireValid();
	}

	public Optional<ProgramSource> getGbuffersBeaconBeam() {
		return gbuffersBeaconBeam.requireValid();
	}

	public Optional<ProgramSource> getGbuffersTextured() {
		return gbuffersTextured.requireValid();
	}

	public Optional<ProgramSource> getGbuffersTexturedLit() {
		return gbuffersTexturedLit.requireValid();
	}

	public Optional<ProgramSource> getGbuffersTerrain() {
		return gbuffersTerrain.requireValid();
	}

	public Optional<ProgramSource> getGbuffersDamagedBlock() {
		return gbuffersDamagedBlock.requireValid();
	}

	public Optional<ProgramSource> getGbuffersWater() {
		return gbuffersWater.requireValid();
	}

	public Optional<ProgramSource> getGbuffersSkyBasic() {
		return gbuffersSkyBasic.requireValid();
	}

	public Optional<ProgramSource> getGbuffersSkyTextured() {
		return gbuffersSkyTextured.requireValid();
	}

	public Optional<ProgramSource> getGbuffersClouds() {
		return gbuffersClouds.requireValid();
	}

	public Optional<ProgramSource> getGbuffersWeather() {
		return gbuffersWeather.requireValid();
	}

	public Optional<ProgramSource> getGbuffersEntities() {
		return gbuffersEntities.requireValid();
	}

	public Optional<ProgramSource> getGbuffersEntitiesGlowing() {
		return gbuffersEntitiesGlowing.requireValid();
	}

	public Optional<ProgramSource> getGbuffersGlint() {
		return gbuffersGlint.requireValid();
	}

	public Optional<ProgramSource> getGbuffersEntityEyes() {
		return gbuffersEntityEyes.requireValid();
	}

	public Optional<ProgramSource> getGbuffersBlock() {
		return gbuffersBlock.requireValid();
	}

	public ProgramSource[] getComposite() {
		return composite;
	}

	public Optional<ProgramSource> getCompositeFinal() {
		return compositeFinal.requireValid();
	}

	public Map<String, Map<String, String>> getLangMap() {
		return langMap;
	}

	public PackDirectives getPackDirectives() {
		return packDirectives;
	}

	private static ProgramSource readProgramSource(Path root, String program, ShaderPack pack, ShaderProperties properties) throws IOException {
		String vertexSource = null;
		String fragmentSource = null;

		try {
			Path vertexPath = root.resolve(program + ".vsh");
			vertexSource = readFile(vertexPath);

			if (vertexSource != null) {
				vertexSource = ShaderPreprocessor.process(root, vertexPath, vertexSource);
			}
		} catch (IOException e) {
			// TODO: Better handling?
			throw e;
		}

		try {
			Path fragmentPath = root.resolve(program + ".fsh");
			fragmentSource = readFile(fragmentPath);

			if (fragmentSource != null) {
				fragmentSource = ShaderPreprocessor.process(root, fragmentPath, fragmentSource);
			}
		} catch (IOException e) {
			// TODO: Better handling?
			throw e;
		}

		return new ProgramSource(program, vertexSource, fragmentSource, pack, properties);
	}

	private static String readFile(Path path) throws IOException {
		try {
			return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
		} catch (FileNotFoundException | NoSuchFileException e) {
			return null;
		}
	}

	private Map<String, Map<String, String>> parseLangEntries(Path root) throws IOException {
		Path langFolderPath = root.resolve("lang");
		Map<String, Map<String, String>> allLanguagesMap = new HashMap<>();

		if (!Files.exists(langFolderPath)) {
			return allLanguagesMap;
		}
		//We are using a max depth of one to ensure we only get the surface level *files* without going deeper
		// we also want to avoid any directories while filtering
		//Basically, we want the immediate files nested in the path for the langFolder
		//There is also Files.list which can be used for similar behavior
		Files.walk(langFolderPath, 1).filter(path -> !Files.isDirectory(path)).forEach(path -> {

			Map<String, String> currentLanguageMap = new HashMap<>();
			//some shaderpacks use optifines file name coding which is different than minecraft's.
			//An example of this is using "en_US.lang" compared to "en_us.json"
			//also note that optifine uses a property scheme for loading language entries to keep parity with other optifine features
			String currentFileName = path.getFileName().toString().toLowerCase();
			String currentLangCode = currentFileName.substring(0, currentFileName.lastIndexOf("."));
			Properties properties = new Properties();

			try {
				properties.load(Files.newInputStream(path));
			} catch (IOException e) {
				Iris.logger.error("Error while parsing languages for shaderpacks! Expected File Path: {}", path);
				Iris.logger.catching(Level.ERROR, e);
			}

			properties.forEach((key, value) -> currentLanguageMap.put(key.toString(), value.toString()));
			allLanguagesMap.put(currentLangCode, currentLanguageMap);
		});

		return allLanguagesMap;
	}

	public static class ProgramSource {
		private final String name;
		private final String vertexSource;
		private final String fragmentSource;
		private final ProgramDirectives directives;
		private final ShaderPack parent;

		public ProgramSource(String name, String vertexSource, String fragmentSource, ShaderPack parent, ShaderProperties properties) {
			this.name = name;
			this.vertexSource = vertexSource;
			this.fragmentSource = fragmentSource;
			this.parent = parent;
			this.directives = new ProgramDirectives(this, properties);
		}

		public String getName() {
			return name;
		}

		public Optional<String> getVertexSource() {
			return Optional.ofNullable(vertexSource);
		}

		public Optional<String> getFragmentSource() {
			return Optional.ofNullable(fragmentSource);
		}

		public ProgramDirectives getDirectives() {
			return this.directives;
		}

		public ShaderPack getParent() {
			return parent;
		}

		public boolean isValid() {
			return vertexSource != null && fragmentSource != null;
		}

		public Optional<ProgramSource> requireValid() {
			if (this.isValid()) {
				return Optional.of(this);
			} else {
				return Optional.empty();
			}
		}
	}
}
