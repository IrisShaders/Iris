package net.coderbot.iris.shaderpack;

import net.coderbot.iris.Iris;
import net.coderbot.iris.gl.blending.BlendMode;
import net.coderbot.iris.gl.blending.BlendModeFunction;
import net.coderbot.iris.gl.blending.BlendModeOverride;
import net.coderbot.iris.shaderpack.include.AbsolutePackPath;
import net.coderbot.iris.shaderpack.loading.ProgramId;
import net.coderbot.iris.vendored.joml.Vector3i;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class ProgramSet {
	private final PackDirectives packDirectives;

	private final ProgramSource shadow;
	private final ComputeSource[] shadowCompute;

	private final ProgramSource[] shadowcomp;
	private final ComputeSource[][] shadowCompCompute;
	private final ProgramSource[] prepare;
	private final ComputeSource[][] prepareCompute;

	private final ProgramSource gbuffersBasic;
	private final ProgramSource gbuffersLine;
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
	private final ProgramSource gbuffersEntitiesTrans;
	private final ProgramSource gbuffersEntitiesGlowing;
	private final ProgramSource gbuffersGlint;
	private final ProgramSource gbuffersEntityEyes;
	private final ProgramSource gbuffersBlock;
	private final ProgramSource gbuffersHand;

	private final ProgramSource[] deferred;
	private final ComputeSource[][] deferredCompute;

	private final ProgramSource gbuffersWater;
	private final ProgramSource gbuffersHandWater;

	private final ProgramSource[] composite;
	private final ComputeSource[][] compositeCompute;
	private final ProgramSource compositeFinal;
	private final ComputeSource[] finalCompute;


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
		this.shadowCompute = readComputeArray(directory, sourceProvider, "shadow");

		this.shadowcomp = readProgramArray(directory, sourceProvider, "shadowcomp", shaderProperties);

		this.shadowCompCompute = new ComputeSource[shadowcomp.length][];
		for (int i = 0; i < shadowcomp.length; i++) {
			this.shadowCompCompute[i] = readComputeArray(directory, sourceProvider, "shadowcomp" + ((i == 0) ? "" : i));
		}

		this.prepare = readProgramArray(directory, sourceProvider, "prepare", shaderProperties);
		this.prepareCompute = new ComputeSource[prepare.length][];
		for (int i = 0; i < prepare.length; i++) {
			this.prepareCompute[i] = readComputeArray(directory, sourceProvider, "prepare" + ((i == 0) ? "" : i));
		}

		this.gbuffersBasic = readProgramSource(directory, sourceProvider, "gbuffers_basic", this, shaderProperties);
		this.gbuffersLine = readProgramSource(directory, sourceProvider, "gbuffers_line", this, shaderProperties);
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
		this.gbuffersEntitiesTrans = readProgramSource(directory, sourceProvider, "gbuffers_entities_translucent", this, shaderProperties);
		this.gbuffersEntitiesGlowing = readProgramSource(directory, sourceProvider, "gbuffers_entities_glowing", this, shaderProperties);
		this.gbuffersGlint = readProgramSource(directory, sourceProvider, "gbuffers_armor_glint", this, shaderProperties);
		this.gbuffersEntityEyes = readProgramSource(directory, sourceProvider, "gbuffers_spidereyes", this, shaderProperties);
		this.gbuffersBlock = readProgramSource(directory, sourceProvider, "gbuffers_block", this, shaderProperties);
		this.gbuffersHand = readProgramSource(directory, sourceProvider, "gbuffers_hand", this, shaderProperties);

		this.deferred = readProgramArray(directory, sourceProvider, "deferred", shaderProperties);
		this.deferredCompute = new ComputeSource[deferred.length][];
		for (int i = 0; i < deferred.length; i++) {
			this.deferredCompute[i] = readComputeArray(directory, sourceProvider, "deferred" + ((i == 0) ? "" : i));
		}

		this.gbuffersWater = readProgramSource(directory, sourceProvider, "gbuffers_water", this, shaderProperties);
		this.gbuffersHandWater = readProgramSource(directory, sourceProvider, "gbuffers_hand_water", this, shaderProperties);

		this.composite = readProgramArray(directory, sourceProvider, "composite", shaderProperties);
		this.compositeCompute = new ComputeSource[composite.length][];
		for (int i = 0; i < deferred.length; i++) {
			this.compositeCompute[i] = readComputeArray(directory, sourceProvider, "composite" + ((i == 0) ? "" : i));
		}
		this.compositeFinal = readProgramSource(directory, sourceProvider, "final", this, shaderProperties);
		this.finalCompute = readComputeArray(directory, sourceProvider, "final");

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

	private ComputeSource[] readComputeArray(AbsolutePackPath directory,
											 Function<AbsolutePackPath, String> sourceProvider, String name) {
		ComputeSource[] programs = new ComputeSource[27];

		programs[0] = readComputeSource(directory, sourceProvider, name, this);

		for (char c = 'a'; c <= 'z'; ++c) {
			String suffix = "_" + c;

			programs[c - 96] = readComputeSource(directory, sourceProvider, name + suffix, this);

			if (programs[c - 96] == null) {
				break;
			}
		}

		return programs;
	}

	private void locateDirectives() {
		List<ProgramSource> programs = new ArrayList<>();
		List<ComputeSource> computes = new ArrayList<>();

		programs.add(shadow);
		programs.addAll(Arrays.asList(shadowcomp));
		programs.addAll(Arrays.asList(prepare));

		programs.addAll (Arrays.asList(
				gbuffersBasic, gbuffersBeaconBeam, gbuffersTextured, gbuffersTexturedLit, gbuffersTerrain,
				gbuffersDamagedBlock, gbuffersSkyBasic, gbuffersSkyTextured, gbuffersClouds, gbuffersWeather,
				gbuffersEntities, gbuffersEntitiesTrans, gbuffersEntitiesGlowing, gbuffersGlint, gbuffersEntityEyes, gbuffersBlock,
				gbuffersHand
		));

		for (ComputeSource[] computeSources : compositeCompute) {
			computes.addAll(Arrays.asList(computeSources));
		}

		for (ComputeSource[] computeSources : deferredCompute) {
			computes.addAll(Arrays.asList(computeSources));
		}

		for (ComputeSource[] computeSources : prepareCompute) {
			computes.addAll(Arrays.asList(computeSources));
		}

		for (ComputeSource[] computeSources : shadowCompCompute) {
			computes.addAll(Arrays.asList(computeSources));
		}

		Collections.addAll(computes, finalCompute);
		Collections.addAll(computes, shadowCompute);

		for (ComputeSource source : computes) {
			if (source != null) {
				source.getSource().map(ConstDirectiveParser::findDirectives).ifPresent(constDirectives -> {
					for (ConstDirectiveParser.ConstDirective directive : constDirectives) {
						if (directive.getType() == ConstDirectiveParser.Type.IVEC3 && directive.getKey().equals("workGroups")) {
							ComputeDirectiveParser.setComputeWorkGroups(source, directive);
						} else if (directive.getType() == ConstDirectiveParser.Type.VEC2 && directive.getKey().equals("workGroupsRender")) {
							ComputeDirectiveParser.setComputeWorkGroupsRelative(source, directive);
						}
					}
				});
			}
		}

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

		packDirectives.getRenderTargetDirectives().getRenderTargetSettings().forEach((index, settings) ->
			Iris.logger.debug("Render target settings for colortex" + index + ": " + settings));
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

	public Optional<ProgramSource> getGbuffersEntitiesTrans() {
		return gbuffersEntitiesTrans.requireValid();
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
			case Line: return gbuffersLine.requireValid();
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
			case EntitiesTrans: return getGbuffersEntitiesTrans();
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

	public ComputeSource[] getShadowCompute() {
		return shadowCompute;
	}

	public ComputeSource[][] getShadowCompCompute() {
		return shadowCompCompute;
	}

	public ComputeSource[][] getPrepareCompute() {
		return prepareCompute;
	}

	public ComputeSource[][] getDeferredCompute() {
		return deferredCompute;
	}

	public ComputeSource[][] getCompositeCompute() {
		return compositeCompute;
	}

	public ComputeSource[] getFinalCompute() {
		return finalCompute;
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

	private static ComputeSource readComputeSource(AbsolutePackPath directory,
												   Function<AbsolutePackPath, String> sourceProvider, String program,
												   ProgramSet programSet) {
		AbsolutePackPath computePath = directory.resolve(program + ".csh");
		String computeSource = sourceProvider.apply(computePath);

		if (computeSource == null) {
			return null;
		}

		return new ComputeSource(program, computeSource, programSet);
	}
}
