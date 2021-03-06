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

	public ProgramSet(Path root, Path inclusionRoot, ShaderPack pack) throws IOException {
		this.packDirectives = new PackDirectives();
		this.pack = pack;

		this.gbuffersBasic = readProgramSource(root, inclusionRoot, "gbuffers_basic", this, pack);
		this.gbuffersBeaconBeam = readProgramSource(root, inclusionRoot, "gbuffers_beaconbeam", this, pack);
		this.gbuffersTextured = readProgramSource(root, inclusionRoot, "gbuffers_textured", this, pack);
		this.gbuffersTexturedLit = readProgramSource(root, inclusionRoot, "gbuffers_textured_lit", this, pack);
		this.gbuffersTerrain = readProgramSource(root, inclusionRoot, "gbuffers_terrain", this, pack);
		this.gbuffersDamagedBlock = readProgramSource(root, inclusionRoot, "gbuffers_damagedblock", this, pack);
		this.gbuffersWater = readProgramSource(root, inclusionRoot, "gbuffers_water", this, pack);
		this.gbuffersSkyBasic = readProgramSource(root, inclusionRoot, "gbuffers_skybasic", this, pack);
		this.gbuffersSkyTextured = readProgramSource(root, inclusionRoot, "gbuffers_skytextured", this, pack);
		this.gbuffersClouds = readProgramSource(root, inclusionRoot, "gbuffers_clouds", this, pack);
		this.gbuffersWeather = readProgramSource(root, inclusionRoot, "gbuffers_weather", this, pack);
		this.gbuffersEntities = readProgramSource(root, inclusionRoot, "gbuffers_entities", this, pack);
		this.gbuffersEntitiesGlowing = readProgramSource(root, inclusionRoot, "gbuffers_entities_glowing", this, pack);
		this.gbuffersGlint = readProgramSource(root, inclusionRoot, "gbuffers_armor_glint", this, pack);
		this.gbuffersEntityEyes = readProgramSource(root, inclusionRoot, "gbuffers_spidereyes", this, pack);
		this.gbuffersBlock = readProgramSource(root, inclusionRoot, "gbuffers_block", this, pack);

		this.composite = new ProgramSource[16];

		for (int i = 0; i < this.composite.length; i++) {
			String suffix = i == 0 ? "" : Integer.toString(i);

			this.composite[i] = readProgramSource(root, inclusionRoot, "composite" + suffix, this, pack);
		}

		this.compositeFinal = readProgramSource(root, inclusionRoot, "final", this, pack);
	}

	private ProgramSet(ProgramSet base, ProgramSet overrides) {
		this.pack = base.pack;

		if (this.pack != overrides.pack) {
			throw new IllegalStateException();
		}

		// TODO: Merge this properly!
		this.packDirectives = base.packDirectives;

		this.gbuffersBasic = merge(base.gbuffersBasic, overrides.gbuffersBasic);
		this.gbuffersBeaconBeam = merge(base.gbuffersBeaconBeam, overrides.gbuffersBeaconBeam);
		this.gbuffersTextured = merge(base.gbuffersTextured, overrides.gbuffersTextured);
		this.gbuffersTexturedLit = merge(base.gbuffersTexturedLit, overrides.gbuffersTexturedLit);
		this.gbuffersTerrain = merge(base.gbuffersTerrain, overrides.gbuffersTerrain);
		this.gbuffersDamagedBlock = merge(base.gbuffersDamagedBlock, overrides.gbuffersDamagedBlock);
		this.gbuffersWater = merge(base.gbuffersWater, overrides.gbuffersWater);
		this.gbuffersSkyBasic = merge(base.gbuffersSkyBasic, overrides.gbuffersSkyBasic);
		this.gbuffersSkyTextured = merge(base.gbuffersSkyTextured, overrides.gbuffersSkyTextured);
		this.gbuffersClouds = merge(base.gbuffersClouds, overrides.gbuffersClouds);
		this.gbuffersWeather = merge(base.gbuffersWeather, overrides.gbuffersWeather);
		this.gbuffersEntities = merge(base.gbuffersEntities, overrides.gbuffersEntities);
		this.gbuffersEntitiesGlowing = merge(base.gbuffersEntitiesGlowing, overrides.gbuffersEntitiesGlowing);
		this.gbuffersGlint = merge(base.gbuffersGlint, overrides.gbuffersGlint);
		this.gbuffersEntityEyes = merge(base.gbuffersEntityEyes, overrides.gbuffersEntityEyes);
		this.gbuffersBlock = merge(base.gbuffersBlock, overrides.gbuffersBlock);

		this.composite = new ProgramSource[16];

		for (int i = 0; i < this.composite.length; i++) {
			String suffix = i == 0 ? "" : Integer.toString(i);

			this.composite[i] = merge(base.composite[i], overrides.composite[i]);
		}

		this.compositeFinal = merge(base.compositeFinal, overrides.compositeFinal);
	}

	private static ProgramSource merge(ProgramSource base, ProgramSource override) {
		if (override != null) {
			return override;
		}

		return base;
	}

	public static ProgramSet merged(ProgramSet base, ProgramSet overrides) {
		if (overrides == null) {
			return base;
		}

		return new ProgramSet(base, overrides);
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

	private static ProgramSource readProgramSource(Path root, Path inclusionRoot, String program, ProgramSet programSet, ShaderPack pack) throws IOException {
		String vertexSource = null;
		String fragmentSource = null;

		try {
			Path vertexPath = root.resolve(program + ".vsh");
			vertexSource = readFile(vertexPath);

			if (vertexSource != null) {
				vertexSource = ShaderPreprocessor.process(inclusionRoot, vertexPath, vertexSource, pack.getConfig());
			}
		} catch (IOException e) {
			// TODO: Better handling?
			throw e;
		}

		try {
			Path fragmentPath = root.resolve(program + ".fsh");
			fragmentSource = readFile(fragmentPath);

			if (fragmentSource != null) {
				fragmentSource = ShaderPreprocessor.process(inclusionRoot, fragmentPath, fragmentSource, pack.getConfig());
			}
		} catch (IOException e) {
			// TODO: Better handling?
			throw e;
		}

		return new ProgramSource(program, vertexSource, fragmentSource, programSet, pack.getShaderProperties());
	}

	private static String readFile(Path path) throws IOException {
		try {
			return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
		} catch (FileNotFoundException | NoSuchFileException e) {
			return null;
		}
	}
}
