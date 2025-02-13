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
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;
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

		ProgramArrayId[] programArrayIds = ProgramArrayId.values();
		ProgramId[] programIds = ProgramId.values();

		ForkJoinTask<ComputeSource[]> readShadowComputeTask = new ReadComputeArrayTask(directory, sourceProvider, "shadow", shaderProperties).fork();
		ForkJoinTask<ComputeSource[]> readSetupTask = new ReadComputeProgramArrayTask(directory, sourceProvider, "setup", shaderProperties).fork();

		EnumMap<ProgramArrayId, ForkJoinTask<ProgramSource[]>> readCompositeProgramTask = new EnumMap<>(ProgramArrayId.class);
		EnumMap<ProgramArrayId, ForkJoinTask<ComputeSource[]>[]> readComputeProgramTask = new EnumMap<>(ProgramArrayId.class);

		for (ProgramArrayId id : programArrayIds) {
			ForkJoinTask<ProgramSource[]> sources = new ReadProgramArrayTask(directory, sourceProvider, id.getSourcePrefix(), shaderProperties, readTesselation).fork();
			readCompositeProgramTask.put(id, sources);
			ForkJoinTask<ComputeSource[]>[] computes = new ForkJoinTask[id.getNumPrograms()];
			for (int i = 0; i < id.getNumPrograms(); i++) {
				computes[i] = new ReadComputeArrayTask(directory, sourceProvider, id.getSourcePrefix() + (i == 0 ? "" : i), shaderProperties).fork();
			}
			readComputeProgramTask.put(id, computes);
		}

		ForkJoinTask<ProgramSource>[] readGbufferProgramTasks = new ForkJoinTask[programIds.length];
		for (ProgramId programId : programIds) {
			readGbufferProgramTasks[programId.ordinal()] = new ReadProgramSourceTask(directory, sourceProvider, programId.getSourceName(), shaderProperties, programId.getBlendModeOverride(), readTesselation).fork();
		}

		ForkJoinTask<ComputeSource[]> readFinalComputeTask = new ReadComputeArrayTask(directory, sourceProvider, "final", shaderProperties).fork();


		this.shadowCompute = readShadowComputeTask.join();
		this.setup = readSetupTask.join();

		for (ProgramArrayId id : ProgramArrayId.values()) {
			ProgramSource[] sources = readCompositeProgramTask.get(id).join();
			compositePrograms.put(id, sources);
			ComputeSource[][] computes = new ComputeSource[id.getNumPrograms()][];
			boolean hasNoComputes = true;
			ForkJoinTask<ComputeSource[]>[] tasks = readComputeProgramTask.get(id);
			for (int i = 0; i < id.getNumPrograms(); i++) {
				computes[i] = tasks[i].join();
				if (computes[i].length > 0) {
					hasNoComputes = false;
				}
			}
			computePrograms.put(id, hasNoComputes ? new ComputeSource[0][] : computes);
		}

		for (ProgramId id : programIds) {
			gbufferPrograms.put(id, readGbufferProgramTasks[id.ordinal()].join());
		}

		this.finalCompute = readFinalComputeTask.join();

		locateDirectives();
	}

	private class ReadComputeProgramArrayTask extends RecursiveTask<ComputeSource[]> {
		private final AbsolutePackPath directory;
		private final Function<AbsolutePackPath, String> sourceProvider;
		private final String name;
		private final ShaderProperties properties;

		private ReadComputeProgramArrayTask(
			AbsolutePackPath directory,
			Function<AbsolutePackPath, String> sourceProvider,
			String name,
			ShaderProperties properties
		) {
			this.directory = directory;
			this.sourceProvider = sourceProvider;
			this.name = name;
			this.properties = properties;
		}

		@Override
		protected ComputeSource[] compute() {
			ForkJoinTask<ComputeSource>[] tasks = new ForkJoinTask[100];

			for (int i = 0; i < tasks.length; i++) {
				String suffix = i == 0 ? "" : Integer.toString(i);

				tasks[i] = new ReadComputeSourceTask(directory, sourceProvider, name + suffix, properties).fork();
			}

			ComputeSource[] programs = new ComputeSource[100];
			for (int i = 0; i < tasks.length; i++) {
				programs[i] = tasks[i].join();
			}

			return programs;
		}
	}

	private class ReadProgramArrayTask extends RecursiveTask<ProgramSource[]> {
		private final AbsolutePackPath directory;
		private final Function<AbsolutePackPath, String> sourceProvider;
		private final String name;
		private final ShaderProperties shaderProperties;
		private final BlendModeOverride blendModeOverride;
		private final boolean readTesselation;

		private ReadProgramArrayTask(AbsolutePackPath directory,
									 Function<AbsolutePackPath, String> sourceProvider, String name,
									 ShaderProperties shaderProperties, BlendModeOverride blendModeOverride, boolean readTesselation) {
			this.directory = directory;
			this.sourceProvider = sourceProvider;
			this.name = name;
			this.shaderProperties = shaderProperties;
			this.blendModeOverride = blendModeOverride;
			this.readTesselation = readTesselation;
		}

		public ReadProgramArrayTask(AbsolutePackPath directory,
									Function<AbsolutePackPath, String> sourceProvider, String name,
									ShaderProperties shaderProperties, boolean readTesselation) {
			this(directory, sourceProvider, name, shaderProperties, null, readTesselation);
		}

		@Override
		protected ProgramSource[] compute() {
			ForkJoinTask<ProgramSource>[] tasks = new ForkJoinTask[100];

			for (int i = 0; i < tasks.length; i++) {
				String suffix = i == 0 ? "" : Integer.toString(i);

				tasks[i] = new ReadProgramSourceTask(directory, sourceProvider, name + suffix, shaderProperties, blendModeOverride, readTesselation).fork();
			}

			ProgramSource[] programs = new ProgramSource[100];
			for (int i = 0; i < tasks.length; i++) {
				programs[i] = tasks[i].join();
			}

			return programs;
		}
	}

	private class ReadComputeArrayTask extends RecursiveTask<ComputeSource[]> {
		private final AbsolutePackPath directory;
		private final Function<AbsolutePackPath, String> sourceProvider;
		private final String name;
		private final ShaderProperties properties;

		private ReadComputeArrayTask(
			AbsolutePackPath directory,
			Function<AbsolutePackPath, String> sourceProvider,
			String name,
			ShaderProperties properties
		) {
			this.directory = directory;
			this.sourceProvider = sourceProvider;
			this.name = name;
			this.properties = properties;
		}

		@Override
		protected ComputeSource[] compute() {
			ForkJoinTask<ComputeSource>[] tasks = new ReadComputeSourceTask[26];

			for (char c = 'a'; c <= 'z'; ++c) {
				String suffix = "_" + c;

				tasks[c - 97] = new ReadComputeSourceTask(directory, sourceProvider, name + suffix, properties).fork();
			}

			ComputeSource[] programs = new ComputeSource[27];
			programs[0] = new ReadComputeSourceTask(directory, sourceProvider, name, properties).compute();

			for (int i = 1; i < 27; i++) {
				programs[i] = tasks[i - 1].join();
				if (programs[i] == null) {
					break;
				}
			}

			if (Arrays.stream(programs).allMatch(Objects::isNull)) {
				return new ComputeSource[0];
			}

			return programs;
		}
	}

	private class ReadProgramSourceTask extends RecursiveTask<ProgramSource> {
		private final AbsolutePackPath directory;
		private final Function<AbsolutePackPath, String> sourceProvider;
		private final String program;
		private final ShaderProperties properties;
		private final BlendModeOverride defaultBlendModeOverride;
		private final boolean readTesselation;

		public ReadProgramSourceTask(AbsolutePackPath directory, Function<AbsolutePackPath, String> sourceProvider, String program, ShaderProperties properties, BlendModeOverride defaultBlendModeOverride, boolean readTesselation) {
			this.directory = directory;
			this.sourceProvider = sourceProvider;
			this.program = program;
			this.properties = properties;
			this.defaultBlendModeOverride = defaultBlendModeOverride;
			this.readTesselation = readTesselation;
		}

		@Override
		protected ProgramSource compute() {
			return readProgramSource(directory, sourceProvider, program, ProgramSet.this, properties, defaultBlendModeOverride, readTesselation);
		}
	}

	private class ReadComputeSourceTask extends RecursiveTask<ComputeSource> {
		private final AbsolutePackPath directory;
		private final Function<AbsolutePackPath, String> sourceProvider;
		private final String program;
		private final ShaderProperties properties;

		public ReadComputeSourceTask(AbsolutePackPath directory,
									 Function<AbsolutePackPath, String> sourceProvider, String program,
									 ShaderProperties properties) {
			this.directory = directory;
			this.sourceProvider = sourceProvider;
			this.program = program;
			this.properties = properties;
		}

		@Override
		protected ComputeSource compute() {
			return readComputeSource(directory, sourceProvider, program, ProgramSet.this, properties);
		}
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
		return computePrograms.getOrDefault(programArrayId, new ComputeSource[0][0]);
	}
}
