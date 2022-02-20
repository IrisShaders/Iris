import io.github.coolcrabs.brachyura.compiler.java.JavaCompilation;
import io.github.coolcrabs.brachyura.compiler.java.JavaCompilationResult;
import io.github.coolcrabs.brachyura.dependency.JavaJarDependency;
import io.github.coolcrabs.brachyura.fabric.FabricProject;
import io.github.coolcrabs.brachyura.ide.IdeModule;
import io.github.coolcrabs.brachyura.mappings.Namespaces;
import io.github.coolcrabs.brachyura.mappings.tinyremapper.MappingTreeMappingProvider;
import io.github.coolcrabs.brachyura.mappings.tinyremapper.RemapperProcessor;
import io.github.coolcrabs.brachyura.processing.ProcessingEntry;
import io.github.coolcrabs.brachyura.processing.ProcessorChain;
import io.github.coolcrabs.brachyura.processing.sinks.AtomicZipProcessingSink;
import io.github.coolcrabs.brachyura.processing.sources.DirectoryProcessingSource;
import io.github.coolcrabs.brachyura.processing.sources.ProcessingSponge;
import io.github.coolcrabs.brachyura.util.JvmUtil;
import io.github.coolcrabs.brachyura.util.Lazy;
import io.github.coolcrabs.brachyura.util.OsUtil;
import io.github.coolcrabs.brachyura.util.PathUtil;
import io.github.coolcrabs.brachyura.util.Util;
import net.fabricmc.mappingio.MappingReader;
import net.fabricmc.mappingio.adapter.MappingSourceNsSwitch;
import net.fabricmc.mappingio.format.MappingFormat;
import net.fabricmc.mappingio.tree.MemoryMappingTree;
import net.fabricmc.tinyremapper.TinyRemapper;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class MultiSrcDirFabricProject extends FabricProject {
	public abstract Path[] paths(String subdir, boolean headers, boolean tocompile);

	@Override
	public JavaJarDependency build() {
		try {
			String mixinOut = "mixinmapout.tiny";
			JavaCompilation compilation = new JavaCompilation()
				.addOption(JvmUtil.compileArgs(JvmUtil.CURRENT_JAVA_VERSION, getJavaVersion()))
				.addOption(
					"-AbrachyuraInMap=" + writeMappings4FabricStuff().toAbsolutePath().toString(),
					"-AbrachyuraOutMap=" + mixinOut, // Remaps shadows etc
					"-AbrachyuraInNamespace=" + Namespaces.NAMED,
					"-AbrachyuraOutNamespace=" + Namespaces.INTERMEDIARY,
					"-AoutRefMapFile=" + getModId() + "-refmap.json", // Remaps annotations
					"-AdefaultObfuscationEnv=brachyura"
				)
				.addClasspath(getCompileDependencies());

			List<Path> headerSourceSets = new ArrayList<>();

			for (Path p : paths("java", false, true)) {
				compilation.addSourceDir(p);
			}
			for (Path p : paths("java", true, false)) {
				compilation.addSourcePathDir(p);
				headerSourceSets.add(p);
			}
			ProcessingSponge compilationOutput = new ProcessingSponge();
			JavaCompilationResult compileResult = compilation.compile();

			compileResult.getInputs((in, id) -> {
				Path srcFile = compileResult.getSourceFile(id);

				if (srcFile != null) {
					for (Path headerSourceSet : headerSourceSets) {
						if (srcFile.startsWith(headerSourceSet)) {
							// Do not write files compiled from the "headers" source set to the final JAR.
							return;
						}
					}
				}

				compilationOutput.sink(in, id);
			});

			MemoryMappingTree compmappings = new MemoryMappingTree(true);
			mappings.get().accept(new MappingSourceNsSwitch(compmappings, Namespaces.NAMED));
			ProcessingEntry mixinMappings = compilationOutput.popEntry(mixinOut);
			if (mixinMappings != null) {
				try (Reader reader = new InputStreamReader(mixinMappings.in.get())) {
					// For easier debugging a seperate tree is made here
					MemoryMappingTree mixinMappingsTree = new MemoryMappingTree();
					MappingReader.read(reader, MappingFormat.TINY_2, mixinMappingsTree);
					mixinMappingsTree.accept(compmappings);
				}
			}
			ProcessingSponge trout = new ProcessingSponge();
			new ProcessorChain(
				new RemapperProcessor(TinyRemapper.newRemapper().withMappings(new MappingTreeMappingProvider(compmappings, Namespaces.NAMED, Namespaces.INTERMEDIARY)), getCompileDependencies())
			).apply(trout, compilationOutput);
			try (AtomicZipProcessingSink out = new AtomicZipProcessingSink(getBuildJarPath())) {
				Path[] resources = paths("resources", false, true);
				DirectoryProcessingSource[] sources = new DirectoryProcessingSource[resources.length];
				for (int i = 0; i < resources.length; i++) {
					sources[i] = new DirectoryProcessingSource(resources[i]);
				}
				resourcesProcessingChain().apply(out, sources);
				trout.getInputs(out);
				out.commit();
			}
			return new JavaJarDependency(getBuildJarPath(), null, getId());
		} catch (Exception e) {
			throw Util.sneak(e);
		}
	}

	@Override
	public IdeModule[] getIdeModules() {
		Path cwd = PathUtil.resolveAndCreateDir(getProjectDir(), "run");
		Lazy<List<Path>> classpath = new Lazy<>(() -> {
			Path mappingsClasspath = writeMappings4FabricStuff().getParent().getParent();
			ArrayList<Path> r = new ArrayList<>(runtimeDependencies.get().size() + 1);
			for (JavaJarDependency dependency : runtimeDependencies.get()) {
				r.add(dependency.jar);
			}
			r.add(mappingsClasspath);
			return r;
		});
		Lazy<Path> launchConfig = new Lazy<>(this::writeLaunchCfg);
		return new IdeModule[] {
			new IdeModule.IdeModuleBuilder()
				.name(getModId())
				.root(getProjectDir())
				.javaVersion(getJavaVersion())
				.dependencies(ideDependencies)
				.sourcePaths(paths("java", true, true))
				.resourcePaths(paths("resources", false, true))
				.runConfigs(
					new IdeModule.RunConfigBuilder()
						.name("Minecraft Client")
						.cwd(cwd)
						.mainClass("net.fabricmc.devlaunchinjector.Main")
						.classpath(classpath)
						.resourcePaths(paths("resources", false, true))
						.vmArgs(
							() -> {
								ArrayList<String> clientArgs = new ArrayList<>(Arrays.asList(
									"-Dfabric.dli.config=" + launchConfig.get().toString(),
									"-Dfabric.dli.env=client",
									"-Dfabric.dli.main=net.fabricmc.loader.launch.knot.KnotClient"
								));
								if (OsUtil.OS == OsUtil.Os.OSX) {
									clientArgs.add("-XstartOnFirstThread");
								}
								return clientArgs;
							}
						),
					new IdeModule.RunConfigBuilder()
						.name("Minecraft Server")
						.cwd(cwd)
						.mainClass("net.fabricmc.devlaunchinjector.Main")
						.classpath(classpath)
						.resourcePaths(getResourcesDir())
						.vmArgs(
							() -> Arrays.asList(
								"-Dfabric.dli.config=" + launchConfig.get().toString(),
								"-Dfabric.dli.env=server",
								"-Dfabric.dli.main=net.fabricmc.loader.launch.knot.KnotServer"
							)
						)
				)
				.build()
		};
	}
}
