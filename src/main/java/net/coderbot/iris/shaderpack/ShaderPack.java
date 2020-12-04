package net.coderbot.iris.shaderpack;

import com.google.common.collect.Maps;
import net.coderbot.iris.Iris;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.*;

public class ShaderPack {
	private final ProgramSource gbuffersBasic;
	private final ProgramSource gbuffersTextured;
	private final ProgramSource gbuffersSkyBasic;
	private final ProgramSource gbuffersSkyTextured;
	private final ProgramSource gbuffersClouds;
	private final IdMap idMap;
	private final Map<String, Map<String, String>> langMap;


	public ShaderPack(Path root) throws IOException {
		this.gbuffersBasic = readProgramSource(root, "gbuffers_basic", this);
		this.gbuffersTextured = readProgramSource(root, "gbuffers_textured", this);
		this.gbuffersSkyBasic = readProgramSource(root, "gbuffers_skybasic", this);
		this.gbuffersSkyTextured = readProgramSource(root, "gbuffers_skytextured", this);
		this.gbuffersClouds = readProgramSource(root, "gbuffers_clouds", this);
		this.idMap = new IdMap(root);
		this.langMap = parseLangEntries(root);
	}

	public IdMap getIdMap() {
		return idMap;
	}

	public Optional<ProgramSource> getGbuffersBasic() {
		return gbuffersBasic.requireValid();
	}

	public Optional<ProgramSource> getGbuffersTextured() {
		return gbuffersTextured.requireValid();
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

	public Map<String, Map<String, String>> getLangMap() {
		return langMap;
	}

	private static ProgramSource readProgramSource(Path root, String program, ShaderPack pack) throws IOException {
		String vertexSource = null;
		String fragmentSource = null;

		try {
			Path vertexPath = root.resolve(program + ".vsh");
			vertexSource = readFile(vertexPath);

			if (vertexSource != null) {
				vertexSource = ShaderPreprocessor.process(vertexPath, vertexSource);
			}
		} catch (IOException e) {
			// TODO: Better handling?
			throw e;
		}

		try {
			Path fragmentPath = root.resolve(program + ".fsh");
			fragmentSource = readFile(fragmentPath);

			if (fragmentSource != null) {
				fragmentSource = ShaderPreprocessor.process(fragmentPath, fragmentSource);
			}
		} catch (IOException e) {
			// TODO: Better handling?
			throw e;
		}

		return new ProgramSource(program, vertexSource, fragmentSource, pack);
	}

	private static String readFile(Path path) throws IOException {
		try {
			return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
		} catch(FileNotFoundException | NoSuchFileException e) {
			return null;
		}
	}

	private Map<String, Map<String, String>> parseLangEntries(Path root) throws IOException {
		Path langFolderPath = root.resolve("lang");
		Map<String, Map<String, String>> allLanguagesMap = new HashMap<>();

		if (!Files.exists(langFolderPath)) {
			return allLanguagesMap;
		}
			Files.walk(langFolderPath, 1).filter(path -> !Files.isDirectory(path)).forEach(path -> {

				Map<String, String> currentLanguageMap = Maps.newHashMap();
				String currentFileName = path.getFileName().toString().toLowerCase();

				String currentLangCode = currentFileName.substring(0, currentFileName.lastIndexOf("."));
				Properties properties = new Properties();

				try {
					properties.load(Files.newInputStream(path));
				} catch (IOException e) {
					Iris.logger.error("Error while parsing languages for shaderpacks!", e);
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
		private final ShaderPack parent;

		public ProgramSource(String name, String vertexSource, String fragmentSource, ShaderPack parent) {
			this.name = name;
			this.vertexSource = vertexSource;
			this.fragmentSource = fragmentSource;
			this.parent = parent;
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
