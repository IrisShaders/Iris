package net.irisshaders.iris.shaderpack.programs;

import net.irisshaders.iris.Iris;
import net.irisshaders.iris.features.FeatureFlags;
import net.irisshaders.iris.gl.blending.BlendModeOverride;
import net.irisshaders.iris.shaderpack.ShaderPack;
import net.irisshaders.iris.shaderpack.include.AbsolutePackPath;
import net.irisshaders.iris.shaderpack.loading.ProgramArrayId;
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
import java.util.EnumMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class ProgramSet implements ProgramSetInterface {
	private final PackDirectives packDirectives;

	private final ComputeSource[] shadowCompute;
	private final ComputeSource[] finalCompute;

	private final ComputeSource[] setup;

	private final ShaderPack pack;

	private final EnumMap<ProgramId, ProgramSource> gbufferPrograms = new EnumMap<>(ProgramId.class);
	private final EnumMap<ProgramArrayId, ProgramSource[]> compositePrograms = new EnumMap<>(ProgramArrayId.class);
	private final EnumMap<ProgramArrayId, ComputeSource[][]> computePrograms = new EnumMap<>(ProgramArrayId.class);

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
		boolean readTesselation = pack.hasFeature(FeatureFlags.TESSELLATION_SHADERS);

		this.shadowCompute = readComputeArray(directory, sourceProvider, "shadow", shaderProperties);
		this.setup = readProgramArray(directory, sourceProvider, "setup", shaderProperties);

		for (ProgramArrayId id : ProgramArrayId.values()) {
			ProgramSource[] sources = readProgramArray(directory, sourceProvider, id.getSourcePrefix(), shaderProperties, readTesselation);
			compositePrograms.put(id, sources);
			ComputeSource[][] computes = new ComputeSource[id.getNumPrograms()][];
			for (int i = 0; i < id.getNumPrograms(); i++) {
				computes[i] = readComputeArray(directory, sourceProvider, id.getSourcePrefix() + (i == 0 ? "" : i), shaderProperties);
			}
			computePrograms.put(id, computes);
		}

		for (ProgramId programId : ProgramId.values()) {
			gbufferPrograms.put(programId, readProgramSource(directory, sourceProvider, programId.getSourceName(), this, shaderProperties, programId.getBlendModeOverride(), readTesselation));
		}

		this.finalCompute = readComputeArray(directory, sourceProvider, "final", shaderProperties);

		locateDirectives();
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

		programs.addAll(Arrays.asList(getComposite(ProgramArrayId.ShadowComposite)));
		programs.addAll(Arrays.asList(getComposite(ProgramArrayId.Begin)));
		programs.addAll(Arrays.asList(getComposite(ProgramArrayId.Prepare)));

		for (ComputeSource[][] sources : computePrograms.values()) {
			for (ComputeSource[] source : sources) {
				computes.addAll(Arrays.asList(source));
			}
		}

		programs.addAll(gbufferPrograms.values());

		for (ComputeSource computeSource : setup) {
			if (computeSource != null) {
				computes.add(computeSource);
			}
		}

		programs.addAll(Arrays.asList(getComposite(ProgramArrayId.Deferred)));
		programs.addAll(Arrays.asList(getComposite(ProgramArrayId.Composite)));

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

	public ComputeSource[] getSetup() {
		return setup;
	}

	public Optional<ProgramSource> get(ProgramId programId) {
		ProgramSource source = gbufferPrograms.getOrDefault(programId, null);
		if (source != null) {
			return source.requireValid();
		} else {
			return Optional.empty();
		}
	}

	public ComputeSource[] getShadowCompute() {
		return shadowCompute;
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

	public ProgramSource[] getComposite(ProgramArrayId programArrayId) {
		return compositePrograms.getOrDefault(programArrayId, new ProgramSource[programArrayId.getNumPrograms()]);
	}

	public ComputeSource[][] getCompute(ProgramArrayId programArrayId) {
		return computePrograms.getOrDefault(programArrayId, new ComputeSource[programArrayId.getNumPrograms()][27]);
	}
}
