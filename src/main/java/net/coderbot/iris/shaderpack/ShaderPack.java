package net.coderbot.iris.shaderpack;

import net.coderbot.iris.gl.program.Program;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Optional;

public class ShaderPack {
	private final ProgramSource gbuffersTextured;
	private final ProgramSource gbuffersClouds;
	private final ProgramSource gbuffersBasic;
	private final Path shaderPackPath;

	public ShaderPack(Path root) throws IOException {
		this.gbuffersTextured = readProgramSource(root, "gbuffers_textured");
		this.gbuffersClouds = readProgramSource(root, "gbuffers_clouds");
		this.gbuffersBasic = readProgramSource(root, "gbuffers_basic");
		this.shaderPackPath = root.resolve("shaders");
	}

	public ProgramSource getGbuffersTextured() {
		return gbuffersTextured;
	}

	public Optional<ProgramSource> getGbuffersClouds() {
		if (gbuffersClouds.isValid()) {
			return Optional.of(gbuffersClouds);
		}

		return Optional.empty();
	}
	public Optional<ProgramSource> getGbuffersBasic(){
		if (gbuffersBasic.isValid()){
			return Optional.of(gbuffersBasic);
		}
		return Optional.empty();
	}
	public Path getPath(){
		return shaderPackPath;
	}

	private static ProgramSource readProgramSource(Path root, String program) throws IOException {
		String vertexSource = null;
		String fragmentSource = null;

		try {
			vertexSource = readFile(root.resolve(program + ".vsh"));
		} catch (IOException e) {
			// TODO: Better handling?
			//throw e;
			//allow handling of fallback shaders if the current shader is not present
		}

		try {
			fragmentSource = readFile(root.resolve(program + ".fsh"));
		} catch (IOException e) {
			// TODO: Better handling?
			//throw e;
			//allow handling of fallback shaders if the current shader is not present
		}

		return new ProgramSource(vertexSource, fragmentSource);
	}

	private static String readFile(Path path) throws IOException {
		try {
			return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
		} catch(FileNotFoundException | NoSuchFileException e) {
			return null;
		}
	}

	public static class ProgramSource {
		private final String vertexSource;
		private final String fragmentSource;

		public ProgramSource(String vertexSource, String fragmentSource) {
			this.vertexSource = vertexSource;
			this.fragmentSource = fragmentSource;
		}

		public Optional<String> getVertexSource() {
			return Optional.ofNullable(vertexSource);
		}

		public Optional<String> getFragmentSource() {
			return Optional.ofNullable(fragmentSource);
		}
		
		public boolean isValid() {
			return vertexSource != null && fragmentSource != null;
		}
	}
}
