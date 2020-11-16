package net.coderbot.iris.shaderpack;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Optional;

public class ShaderPack {
	private final ProgramSource gbuffersBasic;
	private final ProgramSource gbuffersTextured;
	private final ProgramSource gbuffersSkyBasic;
	private final ProgramSource gbuffersSkyTextured;
	private final ProgramSource gbuffersClouds;

	public ShaderPack(Path root) throws IOException {
		this.gbuffersBasic = readProgramSource(root, "gbuffers_basic");
		this.gbuffersTextured = readProgramSource(root, "gbuffers_textured");
		this.gbuffersSkyBasic = readProgramSource(root, "gbuffers_skybasic");
		this.gbuffersSkyTextured = readProgramSource(root, "gbuffers_skytextured");
		this.gbuffersClouds = readProgramSource(root, "gbuffers_clouds");
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


	private static ProgramSource readProgramSource(Path root, String program) throws IOException {
		String vertexSource = null;
		String fragmentSource = null;

		try {
			vertexSource = readFile(root.resolve(program + ".vsh"));
		} catch (IOException e) {
			// TODO: Better handling?
			throw e;
		}

		try {
			fragmentSource = readFile(root.resolve(program + ".fsh"));
		} catch (IOException e) {
			// TODO: Better handling?
			throw e;
		}

		return new ProgramSource(program, vertexSource, fragmentSource);
	}

	private static String readFile(Path path) throws IOException {
		try {
			return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
		} catch(FileNotFoundException | NoSuchFileException e) {
			return null;
		}
	}

	public static class ProgramSource {
		private final String name;
		private final String vertexSource;
		private final String fragmentSource;

		public ProgramSource(String name, String vertexSource, String fragmentSource) {
			this.name = name;
			this.vertexSource = vertexSource;
			this.fragmentSource = fragmentSource;
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
