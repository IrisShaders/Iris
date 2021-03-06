package net.coderbot.iris.shaderpack;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Optional;

public class ProgramSet {
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

	private final ShaderPack pack;
	
	public ProgramSet(Path root, Path inclusionRoot, ShaderProperties shaderProperties, ShaderPack pack) throws IOException {
		this.packDirectives = new PackDirectives();
		this.pack = pack;

		this.gbuffersBasic = readProgramSource(root, inclusionRoot, "gbuffers_basic", this, shaderProperties);
		this.gbuffersBeaconBeam = readProgramSource(root, inclusionRoot, "gbuffers_beaconbeam", this, shaderProperties);
		this.gbuffersTextured = readProgramSource(root, inclusionRoot, "gbuffers_textured", this, shaderProperties);
		this.gbuffersTexturedLit = readProgramSource(root, inclusionRoot, "gbuffers_textured_lit", this, shaderProperties);
		this.gbuffersTerrain = readProgramSource(root, inclusionRoot, "gbuffers_terrain", this, shaderProperties);
		this.gbuffersDamagedBlock = readProgramSource(root, inclusionRoot, "gbuffers_damagedblock", this, shaderProperties);
		this.gbuffersWater = readProgramSource(root, inclusionRoot, "gbuffers_water", this, shaderProperties);
		this.gbuffersSkyBasic = readProgramSource(root, inclusionRoot, "gbuffers_skybasic", this, shaderProperties);
		this.gbuffersSkyTextured = readProgramSource(root, inclusionRoot, "gbuffers_skytextured", this, shaderProperties);
		this.gbuffersClouds = readProgramSource(root, inclusionRoot, "gbuffers_clouds", this, shaderProperties);
		this.gbuffersWeather = readProgramSource(root, inclusionRoot, "gbuffers_weather", this, shaderProperties);
		this.gbuffersEntities = readProgramSource(root, inclusionRoot, "gbuffers_entities", this, shaderProperties);
		this.gbuffersEntitiesGlowing = readProgramSource(root, inclusionRoot, "gbuffers_entities_glowing", this, shaderProperties);
		this.gbuffersGlint = readProgramSource(root, inclusionRoot, "gbuffers_armor_glint", this, shaderProperties);
		this.gbuffersEntityEyes = readProgramSource(root, inclusionRoot, "gbuffers_spidereyes", this, shaderProperties);
		this.gbuffersBlock = readProgramSource(root, inclusionRoot, "gbuffers_block", this, shaderProperties);

		this.composite = new ProgramSource[16];

		for (int i = 0; i < this.composite.length; i++) {
			String suffix = i == 0 ? "" : Integer.toString(i);

			this.composite[i] = readProgramSource(root, inclusionRoot, "composite" + suffix, this, shaderProperties);
		}

		this.compositeFinal = readProgramSource(root, inclusionRoot, "final", this, shaderProperties);
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

	public PackDirectives getPackDirectives() {
		return packDirectives;
	}

	public ShaderPack getPack() {
		return pack;
	}

	private static ProgramSource readProgramSource(Path root, Path inclusionRoot, String program, ProgramSet programSet, ShaderProperties properties) throws IOException {
		String vertexSource = null;
		String fragmentSource = null;

		try {
			Path vertexPath = root.resolve(program + ".vsh");
			vertexSource = readFile(vertexPath);

			if (vertexSource != null) {
				vertexSource = ShaderPreprocessor.process(inclusionRoot, vertexPath, vertexSource);
			}
		} catch (IOException e) {
			// TODO: Better handling?
			throw e;
		}

		try {
			Path fragmentPath = root.resolve(program + ".fsh");
			fragmentSource = readFile(fragmentPath);

			if (fragmentSource != null) {
				fragmentSource = ShaderPreprocessor.process(inclusionRoot, fragmentPath, fragmentSource);
			}
		} catch (IOException e) {
			// TODO: Better handling?
			throw e;
		}

		return new ProgramSource(program, vertexSource, fragmentSource, programSet, properties);
	}

	private static String readFile(Path path) throws IOException {
		try {
			return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
		} catch (FileNotFoundException | NoSuchFileException e) {
			return null;
		}
	}
}
