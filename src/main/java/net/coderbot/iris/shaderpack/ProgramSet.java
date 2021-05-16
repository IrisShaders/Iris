package net.coderbot.iris.shaderpack;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class ProgramSet {
	private final PackDirectives packDirectives;

	private final ProgramSource shadow;

	private final ProgramSource[] shadowcomp;
	private final ProgramSource[] prepare;

	private final ProgramSource gbuffersBasic;
	private final ProgramSource gbuffersBeaconBeam;
	private final ProgramSource gbuffersTextured;
	private final ProgramSource gbuffersTexturedLit;
	private final ProgramSource gbuffersTerrain;
	private final ProgramSource gbuffersDamagedBlock;
	private final ProgramSource gbuffersSkyBasic;
	private final ProgramSource gbuffersSkyTextured;
	private final ProgramSource gbuffersClouds;
	private final ProgramSource gbuffersWeather;
	private final ProgramSource gbuffersEntities;
	private final ProgramSource gbuffersEntitiesGlowing;
	private final ProgramSource gbuffersGlint;
	private final ProgramSource gbuffersEntityEyes;
	private final ProgramSource gbuffersBlock;
	private final ProgramSource gbuffersHand;

	private final ProgramSource[] deferred;

	private final ProgramSource gbuffersWater;
	private final ProgramSource gbuffersHandWater;

	private final ProgramSource[] composite;
	private final ProgramSource compositeFinal;

	private final ShaderPack pack;

	public ProgramSet(Path root, Path inclusionRoot, ShaderProperties shaderProperties, ShaderPack pack) throws IOException {
		this.packDirectives = new PackDirectives();
		this.pack = pack;

		this.shadow = readProgramSource(root, inclusionRoot, "shadow", this, shaderProperties);

		this.shadowcomp = readProgramArray(root, inclusionRoot, "shadowcomp", shaderProperties);
		this.prepare = readProgramArray(root, inclusionRoot, "prepare", shaderProperties);

		this.gbuffersBasic = readProgramSource(root, inclusionRoot, "gbuffers_basic", this, shaderProperties);
		this.gbuffersBeaconBeam = readProgramSource(root, inclusionRoot, "gbuffers_beaconbeam", this, shaderProperties);
		this.gbuffersTextured = readProgramSource(root, inclusionRoot, "gbuffers_textured", this, shaderProperties);
		this.gbuffersTexturedLit = readProgramSource(root, inclusionRoot, "gbuffers_textured_lit", this, shaderProperties);
		this.gbuffersTerrain = readProgramSource(root, inclusionRoot, "gbuffers_terrain", this, shaderProperties);
		this.gbuffersDamagedBlock = readProgramSource(root, inclusionRoot, "gbuffers_damagedblock", this, shaderProperties);
		this.gbuffersSkyBasic = readProgramSource(root, inclusionRoot, "gbuffers_skybasic", this, shaderProperties);
		this.gbuffersSkyTextured = readProgramSource(root, inclusionRoot, "gbuffers_skytextured", this, shaderProperties);
		this.gbuffersClouds = readProgramSource(root, inclusionRoot, "gbuffers_clouds", this, shaderProperties);
		this.gbuffersWeather = readProgramSource(root, inclusionRoot, "gbuffers_weather", this, shaderProperties);
		this.gbuffersEntities = readProgramSource(root, inclusionRoot, "gbuffers_entities", this, shaderProperties);
		this.gbuffersEntitiesGlowing = readProgramSource(root, inclusionRoot, "gbuffers_entities_glowing", this, shaderProperties);
		this.gbuffersGlint = readProgramSource(root, inclusionRoot, "gbuffers_armor_glint", this, shaderProperties);
		this.gbuffersEntityEyes = readProgramSource(root, inclusionRoot, "gbuffers_spidereyes", this, shaderProperties);
		this.gbuffersBlock = readProgramSource(root, inclusionRoot, "gbuffers_block", this, shaderProperties);
		this.gbuffersHand = readProgramSource(root, inclusionRoot, "gbuffers_hand", this, shaderProperties);

		this.deferred = readProgramArray(root, inclusionRoot, "deferred", shaderProperties);

		this.gbuffersWater = readProgramSource(root, inclusionRoot, "gbuffers_water", this, shaderProperties);
		this.gbuffersHandWater = readProgramSource(root, inclusionRoot, "gbuffers_hand_water", this, shaderProperties);

		this.composite = readProgramArray(root, inclusionRoot, "composite", shaderProperties);
		this.compositeFinal = readProgramSource(root, inclusionRoot, "final", this, shaderProperties);

		locateDirectives();
	}

	private ProgramSource[] readProgramArray(Path root, Path inclusionRoot, String name, ShaderProperties shaderProperties) throws IOException {
		ProgramSource[] programs = new ProgramSource[16];

		for (int i = 0; i < programs.length; i++) {
			String suffix = i == 0 ? "" : Integer.toString(i);

			programs[i] = readProgramSource(root, inclusionRoot, name + suffix, this, shaderProperties);
		}

		return programs;
	}

	private ProgramSet(ProgramSet base, ProgramSet overrides) {
		this.pack = base.pack;

		if (this.pack != overrides.pack) {
			throw new IllegalStateException();
		}

		this.packDirectives = new PackDirectives();

		this.shadow = merge(base.shadow, overrides.shadow);

		this.shadowcomp = merge(base.shadowcomp, overrides.shadowcomp);
		this.prepare = merge(base.prepare, overrides.prepare);

		this.gbuffersBasic = merge(base.gbuffersBasic, overrides.gbuffersBasic);
		this.gbuffersBeaconBeam = merge(base.gbuffersBeaconBeam, overrides.gbuffersBeaconBeam);
		this.gbuffersTextured = merge(base.gbuffersTextured, overrides.gbuffersTextured);
		this.gbuffersTexturedLit = merge(base.gbuffersTexturedLit, overrides.gbuffersTexturedLit);
		this.gbuffersTerrain = merge(base.gbuffersTerrain, overrides.gbuffersTerrain);
		this.gbuffersDamagedBlock = merge(base.gbuffersDamagedBlock, overrides.gbuffersDamagedBlock);
		this.gbuffersSkyBasic = merge(base.gbuffersSkyBasic, overrides.gbuffersSkyBasic);
		this.gbuffersSkyTextured = merge(base.gbuffersSkyTextured, overrides.gbuffersSkyTextured);
		this.gbuffersClouds = merge(base.gbuffersClouds, overrides.gbuffersClouds);
		this.gbuffersWeather = merge(base.gbuffersWeather, overrides.gbuffersWeather);
		this.gbuffersEntities = merge(base.gbuffersEntities, overrides.gbuffersEntities);
		this.gbuffersEntitiesGlowing = merge(base.gbuffersEntitiesGlowing, overrides.gbuffersEntitiesGlowing);
		this.gbuffersGlint = merge(base.gbuffersGlint, overrides.gbuffersGlint);
		this.gbuffersEntityEyes = merge(base.gbuffersEntityEyes, overrides.gbuffersEntityEyes);
		this.gbuffersBlock = merge(base.gbuffersBlock, overrides.gbuffersBlock);
		this.gbuffersHand = merge(base.gbuffersHand, overrides.gbuffersHand);

		this.deferred = merge(base.deferred, overrides.deferred);

		this.gbuffersWater = merge(base.gbuffersWater, overrides.gbuffersWater);
		this.gbuffersHandWater = merge(base.gbuffersHandWater, overrides.gbuffersHandWater);

		this.composite = merge(base.composite, overrides.composite);
		this.compositeFinal = merge(base.compositeFinal, overrides.compositeFinal);

		locateDirectives();
	}

	private static ProgramSource[] merge(ProgramSource[] base, ProgramSource[] override) {
		ProgramSource[] merged = new ProgramSource[base.length];

		if (override.length != base.length) {
			throw new IllegalStateException();
		}

		for (int i = 0; i < merged.length; i++) {
			merged[i] = merge(base[i], override[i]);
		}

		return merged;
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

	private void locateDirectives() {
		List<ProgramSource> programs = new ArrayList<>();

		programs.add(shadow);
		programs.addAll(Arrays.asList(shadowcomp));
		programs.addAll(Arrays.asList(prepare));

		programs.addAll (Arrays.asList(
				gbuffersBasic, gbuffersBeaconBeam, gbuffersTextured, gbuffersTexturedLit, gbuffersTerrain,
				gbuffersDamagedBlock, gbuffersSkyBasic, gbuffersSkyTextured, gbuffersClouds, gbuffersWeather,
				gbuffersEntities, gbuffersEntitiesGlowing, gbuffersGlint, gbuffersEntityEyes, gbuffersBlock,
				gbuffersHand
		));

		programs.addAll(Arrays.asList(deferred));
		programs.add(gbuffersWater);
		programs.add(gbuffersHandWater);
		programs.addAll(Arrays.asList(composite));
		programs.add(compositeFinal);

		for (ProgramSource source : programs) {
			if (source == null) {
				continue;
			}

			source
				.getFragmentSource()
				.map(ConstDirectiveParser::findDirectives)
				.ifPresent(lines -> lines.forEach(packDirectives::accept));
		}
	}

	public Optional<ProgramSource> getShadow() {
		return shadow.requireValid();
	}

	public ProgramSource[] getShadowComposite() {
		return shadowcomp;
	}

	public ProgramSource[] getPrepare() {
		return prepare;
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

	public Optional<ProgramSource> getGbuffersHand() {
		return gbuffersHand.requireValid();
	}

	public ProgramSource[] getDeferred() {
		return deferred;
	}

	public Optional<ProgramSource> getGbuffersWater() {
		return gbuffersWater.requireValid();
	}

	public Optional<ProgramSource> getGbuffersHandWater() {
		return gbuffersHandWater.requireValid();
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
		String geometrySource = null;
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
			Path geometryPath = root.resolve(program + ".gsh");
			geometrySource = readFile(geometryPath);

			if (geometrySource != null) {
				geometrySource = ShaderPreprocessor.process(inclusionRoot, geometryPath, geometrySource);
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

		return new ProgramSource(program, vertexSource, geometrySource, fragmentSource, programSet, properties);
	}

	private static String readFile(Path path) throws IOException {
		try {
			return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
		} catch (FileNotFoundException | NoSuchFileException e) {
			return null;
		}
	}
}
