package net.coderbot.iris.shaderpack;

import net.coderbot.iris.Iris;
import net.coderbot.iris.gl.blending.BlendModeOverride;
import net.coderbot.iris.shaderpack.include.AbsolutePackPath;
import net.coderbot.iris.shaderpack.loading.ProgramId;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

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
	private ProgramSource gbuffersDamagedBlock;
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

	public ProgramSet(AbsolutePackPath directory, Function<AbsolutePackPath, String> sourceProvider,
					  ShaderProperties shaderProperties, ShaderPack pack) {
		this.packDirectives = new PackDirectives(PackRenderTargetDirectives.BASELINE_SUPPORTED_RENDER_TARGETS, shaderProperties);
		this.pack = pack;

		// Note: Ensure that blending is properly overridden during the shadow pass. By default, blending is disabled
		//       in the shadow pass. Shader packs expect this for colored shadows from stained glass and nether portals
		//       to work properly.
		//
		// Note: Enabling blending in the shadow pass results in weird results since translucency sorting happens
		//       relative to the player camera, not the shadow camera, so we can't rely on chunks being properly
		//       sorted in the shadow pass.
		//
		// - https://github.com/IrisShaders/Iris/issues/483
		// - https://github.com/IrisShaders/Iris/issues/987
		this.shadow = readProgramSource(directory, sourceProvider, "shadow", this, shaderProperties,
				BlendModeOverride.OFF);

		this.shadowcomp = readProgramArray(directory, sourceProvider, "shadowcomp", shaderProperties);
		this.prepare = readProgramArray(directory, sourceProvider, "prepare", shaderProperties);

		this.gbuffersBasic = readProgramSource(directory, sourceProvider, "gbuffers_basic", this, shaderProperties);
		this.gbuffersBeaconBeam = readProgramSource(directory, sourceProvider, "gbuffers_beaconbeam", this, shaderProperties);
		this.gbuffersTextured = readProgramSource(directory, sourceProvider, "gbuffers_textured", this, shaderProperties);
		this.gbuffersTexturedLit = readProgramSource(directory, sourceProvider, "gbuffers_textured_lit", this, shaderProperties);
		this.gbuffersTerrain = readProgramSource(directory, sourceProvider, "gbuffers_terrain", this, shaderProperties);
		this.gbuffersDamagedBlock = readProgramSource(directory, sourceProvider, "gbuffers_damagedblock", this, shaderProperties);
		this.gbuffersSkyBasic = readProgramSource(directory, sourceProvider, "gbuffers_skybasic", this, shaderProperties);
		this.gbuffersSkyTextured = readProgramSource(directory, sourceProvider, "gbuffers_skytextured", this, shaderProperties);
		this.gbuffersClouds = readProgramSource(directory, sourceProvider, "gbuffers_clouds", this, shaderProperties);
		this.gbuffersWeather = readProgramSource(directory, sourceProvider, "gbuffers_weather", this, shaderProperties);
		this.gbuffersEntities = readProgramSource(directory, sourceProvider, "gbuffers_entities", this, shaderProperties);
		this.gbuffersEntitiesGlowing = readProgramSource(directory, sourceProvider, "gbuffers_entities_glowing", this, shaderProperties);
		this.gbuffersGlint = readProgramSource(directory, sourceProvider, "gbuffers_armor_glint", this, shaderProperties);
		this.gbuffersEntityEyes = readProgramSource(directory, sourceProvider, "gbuffers_spidereyes", this, shaderProperties);
		this.gbuffersBlock = readProgramSource(directory, sourceProvider, "gbuffers_block", this, shaderProperties);
		this.gbuffersHand = readProgramSource(directory, sourceProvider, "gbuffers_hand", this, shaderProperties);

		this.deferred = readProgramArray(directory, sourceProvider, "deferred", shaderProperties);

		this.gbuffersWater = readProgramSource(directory, sourceProvider, "gbuffers_water", this, shaderProperties);
		this.gbuffersHandWater = readProgramSource(directory, sourceProvider, "gbuffers_hand_water", this, shaderProperties);

		this.composite = readProgramArray(directory, sourceProvider, "composite", shaderProperties);
		this.compositeFinal = readProgramSource(directory, sourceProvider, "final", this, shaderProperties);

		locateDirectives();

		if (!gbuffersDamagedBlock.isValid()) {
			// Special behavior inherited by OptiFine & Iris from old ShadersMod
			// Presumably this was added before DRAWBUFFERS was a thing? Or just a hardcoded hacky fix for some
			// shader packs - in any case, Sildurs Vibrant Shaders and other packs rely on it.
			first(getGbuffersTerrain(), getGbuffersTexturedLit(), getGbuffersTextured(), getGbuffersBasic()).ifPresent(src -> {
				ProgramDirectives overrideDirectives = src.getDirectives().withOverriddenDrawBuffers(new int[] { 0 });
				this.gbuffersDamagedBlock = src.withDirectiveOverride(overrideDirectives);
			});
		}
	}

	@SafeVarargs
	private static <T> Optional<T> first(Optional<T>... candidates) {
		for (Optional<T> candidate : candidates) {
			if (candidate.isPresent()) {
				return candidate;
			}
		}

		return Optional.empty();
	}

	private ProgramSource[] readProgramArray(AbsolutePackPath directory,
											 Function<AbsolutePackPath, String> sourceProvider, String name,
											 ShaderProperties shaderProperties) {
		ProgramSource[] programs = new ProgramSource[99];

		for (int i = 0; i < programs.length; i++) {
			String suffix = i == 0 ? "" : Integer.toString(i);

			programs[i] = readProgramSource(directory, sourceProvider, name + suffix, this, shaderProperties);
		}

		return programs;
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

		DispatchingDirectiveHolder packDirectiveHolder = new DispatchingDirectiveHolder();

		packDirectives.acceptDirectivesFrom(packDirectiveHolder);

		for (ProgramSource source : programs) {
			if (source == null) {
				continue;
			}

			source.getFragmentSource().map(ConstDirectiveParser::findDirectives).ifPresent(directives -> {
				for (ConstDirectiveParser.ConstDirective directive : directives) {
					packDirectiveHolder.processDirective(directive);
				}
			});
		}

		packDirectives.getRenderTargetDirectives().getRenderTargetSettings().forEach((index, settings) -> {
			Iris.logger.debug("Render target settings for colortex" + index + ": " + settings);
		});
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

	public Optional<ProgramSource> get(ProgramId programId) {
		switch (programId) {
			case Shadow: return getShadow();
			case Basic: return getGbuffersBasic();
			//case Line: return Optional.empty();
			case Textured: return getGbuffersTextured();
			case TexturedLit: return getGbuffersTexturedLit();
			case SkyBasic: return getGbuffersSkyBasic();
			case SkyTextured: return getGbuffersSkyTextured();
			case Clouds: return getGbuffersClouds();
			case Terrain: return getGbuffersTerrain();
			case DamagedBlock: return getGbuffersDamagedBlock();
			case Block: return getGbuffersBlock();
			case BeaconBeam: return getGbuffersBeaconBeam();
			case Entities: return getGbuffersEntities();
			case EntitiesGlowing: return getGbuffersEntitiesGlowing();
			case ArmorGlint: return getGbuffersGlint();
			case SpiderEyes: return getGbuffersEntityEyes();
			case Hand: return getGbuffersHand();
			case Weather: return getGbuffersWeather();
			case Water: return getGbuffersWater();
			case HandWater: return getGbuffersHandWater();
			case Final: return getCompositeFinal();
			default: return Optional.empty();
		}
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

	private static ProgramSource readProgramSource(AbsolutePackPath directory,
												   Function<AbsolutePackPath, String> sourceProvider, String program,
												   ProgramSet programSet, ShaderProperties properties) {
		return readProgramSource(directory, sourceProvider, program, programSet, properties, null);
	}

	private static ProgramSource readProgramSource(AbsolutePackPath directory,
												   Function<AbsolutePackPath, String> sourceProvider, String program,
												   ProgramSet programSet, ShaderProperties properties,
												   BlendModeOverride defaultBlendModeOverride) {
		AbsolutePackPath vertexPath = directory.resolve(program + ".vsh");
		String vertexSource = sourceProvider.apply(vertexPath);

		AbsolutePackPath geometryPath = directory.resolve(program + ".gsh");
		String geometrySource = sourceProvider.apply(geometryPath);

		AbsolutePackPath fragmentPath = directory.resolve(program + ".fsh");
		String fragmentSource = sourceProvider.apply(fragmentPath);

		return new ProgramSource(program, vertexSource, geometrySource, fragmentSource, programSet, properties,
				defaultBlendModeOverride);
	}
}
