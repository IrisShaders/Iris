package net.irisshaders.iris.shaderpack.programs;

import net.irisshaders.iris.Iris;
import net.irisshaders.iris.features.FeatureFlags;
import net.irisshaders.iris.gl.blending.BlendModeOverride;
import net.irisshaders.iris.shaderpack.ShaderPack;
import net.irisshaders.iris.shaderpack.include.AbsolutePackPath;
import net.irisshaders.iris.shaderpack.loading.ProgramId;
import net.irisshaders.iris.shaderpack.parsing.ComputeDirectiveParser;
import net.irisshaders.iris.shaderpack.parsing.ConstDirectiveParser;
import net.irisshaders.iris.shaderpack.parsing.DispatchingDirectiveHolder;
import net.irisshaders.iris.shaderpack.properties.PackDirectives;
import net.irisshaders.iris.shaderpack.properties.PackRenderTargetDirectives;
import net.irisshaders.iris.shaderpack.properties.ShaderProperties;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class ProgramSet implements ProgramSetInterface {
	private final PackDirectives packDirectives;

	private final ProgramSource shadow;
	private final ComputeSource[] shadowCompute;

	private final ProgramSource[] shadowcomp;
	private final ComputeSource[][] shadowCompCompute;
	private final ProgramSource[] begin;
	private final ComputeSource[][] beginCompute;
	private final ProgramSource[] prepare;
	private final ComputeSource[][] prepareCompute;
	private final ComputeSource[] setup;

	private final ProgramSource gbuffersBasic;
	private final ProgramSource gbuffersLine;
	private final ProgramSource gbuffersBeaconBeam;
	private final ProgramSource gbuffersTextured;
	private final ProgramSource gbuffersTexturedLit;
	private final ProgramSource gbuffersTerrain;
	private final ProgramSource gbuffersTerrainSolid;
	private final ProgramSource gbuffersTerrainCutout;
	private final ProgramSource gbuffersSkyBasic;
	private final ProgramSource gbuffersSkyTextured;
	private final ProgramSource gbuffersClouds;
	private final ProgramSource gbuffersWeather;
	private final ProgramSource gbuffersEntities;
	private final ProgramSource gbuffersEntitiesTrans;
	private final ProgramSource gbuffersParticles;
	private final ProgramSource gbuffersParticlesTrans;
	private final ProgramSource gbuffersEntitiesGlowing;
	private final ProgramSource gbuffersGlint;
	private final ProgramSource gbuffersEntityEyes;
	private final ProgramSource gbuffersBlock;
	private final ProgramSource gbuffersBlockTrans;
	private final ProgramSource gbuffersHand;
	private final ProgramSource[] deferred;
	private final ComputeSource[][] deferredCompute;
	private final ProgramSource gbuffersWater;
	private final ProgramSource gbuffersHandWater;
	private final ProgramSource[] composite;
	private final ComputeSource[][] compositeCompute;
	private final ProgramSource compositeFinal;
	private final ComputeSource[] finalCompute;
	private final ProgramSource dhTerrain;
	private final ProgramSource dhWater;
	private final ProgramSource dhShadow;
	private final ShaderPack pack;
	private final ProgramSource gbuffersDamagedBlock;

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
		boolean readTesselation = pack.hasFeature(FeatureFlags.TESSELATION_SHADERS);

		this.shadow = readProgramSource(directory, sourceProvider, "shadow", this, shaderProperties,
			BlendModeOverride.OFF, readTesselation);
		this.shadowCompute = readComputeArray(directory, sourceProvider, "shadow", shaderProperties);

		this.shadowcomp = readProgramArray(directory, sourceProvider, "shadowcomp", shaderProperties, readTesselation);

		this.shadowCompCompute = new ComputeSource[shadowcomp.length][];
		for (int i = 0; i < shadowcomp.length; i++) {
			this.shadowCompCompute[i] = readComputeArray(directory, sourceProvider, "shadowcomp" + ((i == 0) ? "" : i), shaderProperties);
		}

		this.setup = readProgramArray(directory, sourceProvider, "setup", shaderProperties);

		this.begin = readProgramArray(directory, sourceProvider, "begin", shaderProperties, readTesselation);
		this.beginCompute = new ComputeSource[begin.length][];
		for (int i = 0; i < begin.length; i++) {
			this.beginCompute[i] = readComputeArray(directory, sourceProvider, "begin" + ((i == 0) ? "" : i), shaderProperties);
		}

		this.prepare = readProgramArray(directory, sourceProvider, "prepare", shaderProperties, readTesselation);
		this.prepareCompute = new ComputeSource[prepare.length][];
		for (int i = 0; i < prepare.length; i++) {
			this.prepareCompute[i] = readComputeArray(directory, sourceProvider, "prepare" + ((i == 0) ? "" : i), shaderProperties);
		}

		this.gbuffersBasic = readProgramSource(directory, sourceProvider, "gbuffers_basic", this, shaderProperties, readTesselation);
		this.gbuffersLine = readProgramSource(directory, sourceProvider, "gbuffers_line", this, shaderProperties, readTesselation);
		this.gbuffersBeaconBeam = readProgramSource(directory, sourceProvider, "gbuffers_beaconbeam", this, shaderProperties, readTesselation);
		this.gbuffersTextured = readProgramSource(directory, sourceProvider, "gbuffers_textured", this, shaderProperties, readTesselation);
		this.gbuffersTexturedLit = readProgramSource(directory, sourceProvider, "gbuffers_textured_lit", this, shaderProperties, readTesselation);
		this.gbuffersTerrain = readProgramSource(directory, sourceProvider, "gbuffers_terrain", this, shaderProperties, readTesselation);
		this.gbuffersTerrainSolid = readProgramSource(directory, sourceProvider, "gbuffers_terrain_solid", this, shaderProperties, readTesselation);
		this.gbuffersTerrainCutout = readProgramSource(directory, sourceProvider, "gbuffers_terrain_cutout", this, shaderProperties, readTesselation);
		this.gbuffersDamagedBlock = readProgramSource(directory, sourceProvider, "gbuffers_damagedblock", this, shaderProperties, readTesselation);
		this.gbuffersSkyBasic = readProgramSource(directory, sourceProvider, "gbuffers_skybasic", this, shaderProperties, readTesselation);
		this.gbuffersSkyTextured = readProgramSource(directory, sourceProvider, "gbuffers_skytextured", this, shaderProperties, readTesselation);
		this.gbuffersClouds = readProgramSource(directory, sourceProvider, "gbuffers_clouds", this, shaderProperties, readTesselation);
		this.gbuffersWeather = readProgramSource(directory, sourceProvider, "gbuffers_weather", this, shaderProperties, readTesselation);
		this.gbuffersEntities = readProgramSource(directory, sourceProvider, "gbuffers_entities", this, shaderProperties, readTesselation);
		this.gbuffersEntitiesTrans = readProgramSource(directory, sourceProvider, "gbuffers_entities_translucent", this, shaderProperties, readTesselation);
		this.gbuffersParticles = readProgramSource(directory, sourceProvider, "gbuffers_particles", this, shaderProperties, readTesselation);
		this.gbuffersParticlesTrans = readProgramSource(directory, sourceProvider, "gbuffers_particles_translucent", this, shaderProperties, readTesselation);
		this.gbuffersEntitiesGlowing = readProgramSource(directory, sourceProvider, "gbuffers_entities_glowing", this, shaderProperties, readTesselation);
		this.gbuffersGlint = readProgramSource(directory, sourceProvider, "gbuffers_armor_glint", this, shaderProperties, readTesselation);
		this.gbuffersEntityEyes = readProgramSource(directory, sourceProvider, "gbuffers_spidereyes", this, shaderProperties, readTesselation);
		this.gbuffersBlock = readProgramSource(directory, sourceProvider, "gbuffers_block", this, shaderProperties, readTesselation);
		this.gbuffersBlockTrans = readProgramSource(directory, sourceProvider, "gbuffers_block_translucent", this, shaderProperties, readTesselation);
		this.gbuffersHand = readProgramSource(directory, sourceProvider, "gbuffers_hand", this, shaderProperties, readTesselation);
		this.dhTerrain = readProgramSource(directory, sourceProvider, "dh_terrain", this, shaderProperties, readTesselation);
		this.dhWater = readProgramSource(directory, sourceProvider, "dh_water", this, shaderProperties, readTesselation);
		this.dhShadow = readProgramSource(directory, sourceProvider, "dh_shadow", this, shaderProperties, readTesselation);

		this.deferred = readProgramArray(directory, sourceProvider, "deferred", shaderProperties, readTesselation);
		this.deferredCompute = new ComputeSource[deferred.length][];
		for (int i = 0; i < deferred.length; i++) {
			this.deferredCompute[i] = readComputeArray(directory, sourceProvider, "deferred" + ((i == 0) ? "" : i), shaderProperties);
		}

		this.gbuffersWater = readProgramSource(directory, sourceProvider, "gbuffers_water", this, shaderProperties, readTesselation);
		this.gbuffersHandWater = readProgramSource(directory, sourceProvider, "gbuffers_hand_water", this, shaderProperties, readTesselation);

		this.composite = readProgramArray(directory, sourceProvider, "composite", shaderProperties, readTesselation);
		this.compositeCompute = new ComputeSource[composite.length][];
		for (int i = 0; i < deferred.length; i++) {
			this.compositeCompute[i] = readComputeArray(directory, sourceProvider, "composite" + ((i == 0) ? "" : i), shaderProperties);
		}
		this.compositeFinal = readProgramSource(directory, sourceProvider, "final", this, shaderProperties, readTesselation);
		this.finalCompute = readComputeArray(directory, sourceProvider, "final", shaderProperties);

		locateDirectives();
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

	private static ProgramSource readProgramSource(AbsolutePackPath directory,
												   Function<AbsolutePackPath, String> sourceProvider, String program,
												   ProgramSet programSet, ShaderProperties properties, boolean readTesselation) {
		return readProgramSource(directory, sourceProvider, program, programSet, properties, null, readTesselation);
	}

	private static ProgramSource readProgramSource(AbsolutePackPath directory,
												   Function<AbsolutePackPath, String> sourceProvider, String program,
												   ProgramSet programSet, ShaderProperties properties,
												   BlendModeOverride defaultBlendModeOverride, boolean readTesselation) {
		AbsolutePackPath vertexPath = directory.resolve(program + ".vsh");
		String vertexSource = sourceProvider.apply(vertexPath);

		AbsolutePackPath geometryPath = directory.resolve(program + ".gsh");
		String geometrySource = sourceProvider.apply(geometryPath);

		String tessControlSource = null;
		String tessEvalSource = null;

		if (readTesselation) {
			AbsolutePackPath tessControlPath = directory.resolve(program + ".tcs");
			tessControlSource = sourceProvider.apply(tessControlPath);

			AbsolutePackPath tessEvalPath = directory.resolve(program + ".tes");
			tessEvalSource = sourceProvider.apply(tessEvalPath);
		}

		AbsolutePackPath fragmentPath = directory.resolve(program + ".fsh");
		String fragmentSource = sourceProvider.apply(fragmentPath);

		if (vertexSource == null && fragmentSource != null) {
			// This is for really old packs that do not use a vertex shader.
			Iris.logger.warn("Found a program (" + program + ") that has a fragment shader but no vertex shader? This is very legacy behavior and might not work right.");
			vertexSource = """
				#version 120

				varying vec4 irs_texCoords[3];
				varying vec4 irs_Color;

				void main() {
					gl_Position = ftransform();
					irs_texCoords[0] = gl_TextureMatrix[0] * gl_MultiTexCoord0;
					irs_texCoords[1] = gl_TextureMatrix[1] * gl_MultiTexCoord1;
					irs_texCoords[2] = gl_TextureMatrix[1] * gl_MultiTexCoord2;
					irs_Color = gl_Color;
				}
				""";
		}

		return new ProgramSource(program, vertexSource, geometrySource, tessControlSource, tessEvalSource, fragmentSource, programSet, properties,
			defaultBlendModeOverride);
	}

	private static ComputeSource readComputeSource(AbsolutePackPath directory,
												   Function<AbsolutePackPath, String> sourceProvider, String program,
												   ProgramSet programSet, ShaderProperties properties) {
		AbsolutePackPath computePath = directory.resolve(program + ".csh");
		String computeSource = sourceProvider.apply(computePath);

		if (computeSource == null) {
			return null;
		}

		return new ComputeSource(program, computeSource, programSet, properties);
	}

	private ProgramSource[] readProgramArray(AbsolutePackPath directory,
											 Function<AbsolutePackPath, String> sourceProvider, String name,
											 ShaderProperties shaderProperties, boolean readTesselation) {
		ProgramSource[] programs = new ProgramSource[100];

		for (int i = 0; i < programs.length; i++) {
			String suffix = i == 0 ? "" : Integer.toString(i);

			programs[i] = readProgramSource(directory, sourceProvider, name + suffix, this, shaderProperties, readTesselation);
		}

		return programs;
	}

	private ComputeSource[] readProgramArray(AbsolutePackPath directory,
											 Function<AbsolutePackPath, String> sourceProvider, String name, ShaderProperties properties) {
		ComputeSource[] programs = new ComputeSource[100];

		for (int i = 0; i < programs.length; i++) {
			String suffix = i == 0 ? "" : Integer.toString(i);

			programs[i] = readComputeSource(directory, sourceProvider, name + suffix, this, properties);
		}

		return programs;
	}

	private ComputeSource[] readComputeArray(AbsolutePackPath directory,
											 Function<AbsolutePackPath, String> sourceProvider, String name, ShaderProperties properties) {
		ComputeSource[] programs = new ComputeSource[27];

		programs[0] = readComputeSource(directory, sourceProvider, name, this, properties);

		for (char c = 'a'; c <= 'z'; ++c) {
			String suffix = "_" + c;

			programs[c - 96] = readComputeSource(directory, sourceProvider, name + suffix, this, properties);

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
		programs.addAll(Arrays.asList(begin));
		programs.addAll(Arrays.asList(prepare));

		programs.addAll(Arrays.asList(
			gbuffersBasic, gbuffersBeaconBeam, gbuffersTextured, gbuffersTexturedLit, gbuffersTerrain, gbuffersTerrainSolid, gbuffersTerrainCutout,
			gbuffersDamagedBlock, gbuffersSkyBasic, gbuffersSkyTextured, gbuffersClouds, gbuffersWeather,
			gbuffersEntities, gbuffersEntitiesTrans, gbuffersEntitiesGlowing, gbuffersGlint, gbuffersEntityEyes, gbuffersBlock, gbuffersBlockTrans,
			gbuffersHand, dhShadow, dhTerrain, dhWater
		));

		for (ComputeSource computeSource : setup) {
			if (computeSource != null) {
				computes.add(computeSource);
			}
		}

		for (ComputeSource[] computeSources : beginCompute) {
			computes.addAll(Arrays.asList(computeSources));
		}

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

	public ProgramSource[] getBegin() {
		return begin;
	}

	public ProgramSource[] getPrepare() {
		return prepare;
	}

	public ComputeSource[] getSetup() {
		return setup;
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

	public Optional<ProgramSource> getGbuffersTerrainSolid() {
		return gbuffersTerrainSolid.requireValid();
	}

	public Optional<ProgramSource> getGbuffersTerrainCutout() {
		return gbuffersTerrainCutout.requireValid();
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

	public Optional<ProgramSource> getGbuffersParticles() {
		return gbuffersParticles.requireValid();
	}

	public Optional<ProgramSource> getGbuffersParticlesTrans() {
		return gbuffersParticlesTrans.requireValid();
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

	public Optional<ProgramSource> getGbuffersBlockTrans() {
		return gbuffersBlockTrans.requireValid();
	}

	public Optional<ProgramSource> getGbuffersHand() {
		return gbuffersHand.requireValid();
	}

	public Optional<ProgramSource> getDhTerrain() {
		return dhTerrain.requireValid();
	}

	public Optional<ProgramSource> getDhWater() {
		return dhWater.requireValid();
	}

	public Optional<ProgramSource> getDhShadow() {
		return dhShadow.requireValid();
	}

	public Optional<ProgramSource> get(ProgramId programId) {
		return switch (programId) {
			case Shadow -> getShadow();
			case Basic -> getGbuffersBasic();
			case Line -> gbuffersLine.requireValid();
			case Textured -> getGbuffersTextured();
			case TexturedLit -> getGbuffersTexturedLit();
			case SkyBasic -> getGbuffersSkyBasic();
			case SkyTextured -> getGbuffersSkyTextured();
			case Clouds -> getGbuffersClouds();
			case Terrain -> getGbuffersTerrain();
			case TerrainSolid -> getGbuffersTerrainSolid();
			case TerrainCutout -> getGbuffersTerrainCutout();
			case DamagedBlock -> getGbuffersDamagedBlock();
			case Block -> getGbuffersBlock();
			case BlockTrans -> getGbuffersBlockTrans();
			case BeaconBeam -> getGbuffersBeaconBeam();
			case Entities -> getGbuffersEntities();
			case EntitiesTrans -> getGbuffersEntitiesTrans();
			case Particles -> getGbuffersParticles();
			case ParticlesTrans -> getGbuffersParticlesTrans();
			case EntitiesGlowing -> getGbuffersEntitiesGlowing();
			case ArmorGlint -> getGbuffersGlint();
			case SpiderEyes -> getGbuffersEntityEyes();
			case Hand -> getGbuffersHand();
			case Weather -> getGbuffersWeather();
			case Water -> getGbuffersWater();
			case HandWater -> getGbuffersHandWater();
			case Final -> getCompositeFinal();
			case DhTerrain -> getDhTerrain();
			case DhWater -> getDhWater();
			case DhShadow -> getDhShadow();
			default -> Optional.empty();
		};
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

	public ComputeSource[][] getBeginCompute() {
		return beginCompute;
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
}
