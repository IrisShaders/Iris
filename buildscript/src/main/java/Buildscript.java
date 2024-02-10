import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import io.github.coolcrabs.brachyura.compiler.java.JavaCompilation;
import io.github.coolcrabs.brachyura.compiler.java.JavaCompilationResult;
import io.github.coolcrabs.brachyura.decompiler.BrachyuraDecompiler;
import io.github.coolcrabs.brachyura.decompiler.fernflower.FernflowerDecompiler;
import io.github.coolcrabs.brachyura.dependency.JavaJarDependency;
import io.github.coolcrabs.brachyura.fabric.FabricContext;
import io.github.coolcrabs.brachyura.fabric.FabricLoader;
import io.github.coolcrabs.brachyura.fabric.FabricMaven;
import io.github.coolcrabs.brachyura.fabric.FabricModule;
import io.github.coolcrabs.brachyura.fabric.SimpleFabricProject;
import io.github.coolcrabs.brachyura.fabric.FabricContext.ModDependencyCollector;
import io.github.coolcrabs.brachyura.fabric.FabricContext.ModDependencyFlag;
import io.github.coolcrabs.brachyura.mappings.Namespaces;
import io.github.coolcrabs.brachyura.maven.Maven;
import io.github.coolcrabs.brachyura.maven.MavenId;
import io.github.coolcrabs.brachyura.minecraft.Minecraft;
import io.github.coolcrabs.brachyura.minecraft.VersionMeta;
import io.github.coolcrabs.brachyura.processing.ProcessingEntry;
import io.github.coolcrabs.brachyura.processing.ProcessingSink;
import io.github.coolcrabs.brachyura.processing.ProcessorChain;
import io.github.coolcrabs.brachyura.processing.sources.ProcessingSponge;
import io.github.coolcrabs.brachyura.project.java.BuildModule;
import io.github.coolcrabs.brachyura.quilt.QuiltMaven;
import io.github.coolcrabs.brachyura.util.JvmUtil;
import io.github.coolcrabs.brachyura.util.Lazy;
import io.github.coolcrabs.brachyura.util.Util;
import net.fabricmc.mappingio.MappingReader;
import net.fabricmc.mappingio.format.MappingFormat;
import net.fabricmc.mappingio.tree.MappingTree;
import net.fabricmc.mappingio.tree.MemoryMappingTree;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.RepositoryNotFoundException;
import org.eclipse.jgit.lib.Constants;

public class Buildscript extends SimpleFabricProject {
	static final boolean SODIUM = true;
	static final boolean CUSTOM_SODIUM = false;
	static final String MC_VERSION = "1.20.1";
	static final String customSodiumName = "sodium-fabric-mc1.20.3-0.5.6git.7a62284.jar";

	private static final String[] SOURCE_SETS = new String[] {
		"main",
		"vendored",
		SODIUM ? "sodiumCompatibility" : "noSodiumStub",
		"headers"
	};

	private static final String[] HEADER_SOURCE_SETS = new String[] {
		"headers"
	};

	@Override
	public VersionMeta createMcVersion() {
		return Minecraft.getVersion(MC_VERSION);
	}

	@Override
	public MappingTree createMappings() {
		return createMojmap();
	}

	@Override
	public FabricLoader getLoader() {
		return new FabricLoader(FabricMaven.URL, FabricMaven.loader("0.14.24"));
	}

	@Override
	public int getJavaVersion() {
		return 17;
	}

	@Override
	public Path getSrcDir() {
		throw new UnsupportedOperationException();
	}

	@Override
	protected FabricModule createModule() {
		return new IrisFabricModule(context.get());
	}

	@Override
	public BrachyuraDecompiler decompiler() {
		return new FernflowerDecompiler(Maven.getMavenJarDep(QuiltMaven.URL, new MavenId("org.quiltmc", "quiltflower", "1.9.0")));
	}

	@Override
	public void getModDependencies(ModDependencyCollector d) {
		jij(d.addMaven(Maven.MAVEN_CENTRAL, new MavenId("org.anarres:jcpp:1.4.14"), ModDependencyFlag.COMPILE, ModDependencyFlag.RUNTIME));
		jij(d.addMaven(FabricMaven.URL, new MavenId(FabricMaven.GROUP_ID + ".fabric-api", "fabric-key-binding-api-v1", "1.0.23+aaaf9d332d"), ModDependencyFlag.COMPILE, ModDependencyFlag.RUNTIME));

		jij(d.addMaven(Maven.MAVEN_CENTRAL, new MavenId("io.github.douira:glsl-transformer:2.0.0-pre13"), ModDependencyFlag.COMPILE, ModDependencyFlag.RUNTIME));
		jij(d.addMaven(Maven.MAVEN_CENTRAL, new MavenId("org.antlr:antlr4-runtime:4.11.1"), ModDependencyFlag.COMPILE, ModDependencyFlag.RUNTIME));
		d.addMaven("https://api.modrinth.com/maven", new MavenId("maven.modrinth", "distanthorizons", "2.0.0-a-1.18.2"), ModDependencyFlag.COMPILE);

		if (SODIUM) {
			d.addMaven(FabricMaven.URL, new MavenId(FabricMaven.GROUP_ID + ".fabric-api", "fabric-api-base", "0.4.17+93d8cb8253"), ModDependencyFlag.COMPILE, ModDependencyFlag.RUNTIME);
			d.addMaven(FabricMaven.URL, new MavenId(FabricMaven.GROUP_ID + ".fabric-api", "fabric-rendering-data-attachment-v1", "0.3.21+12bfe4ea53"), ModDependencyFlag.COMPILE, ModDependencyFlag.RUNTIME);
			d.addMaven(FabricMaven.URL, new MavenId(FabricMaven.GROUP_ID + ".fabric-api", "fabric-block-view-api-v2", "1.0.0+92a0d36777"), ModDependencyFlag.COMPILE, ModDependencyFlag.RUNTIME);
			d.addMaven(FabricMaven.URL, new MavenId(FabricMaven.GROUP_ID + ".fabric-api", "fabric-rendering-fluids-v1", "3.0.13+fbde993d53"), ModDependencyFlag.COMPILE, ModDependencyFlag.RUNTIME);
			d.addMaven(FabricMaven.URL, new MavenId(FabricMaven.GROUP_ID + ".fabric-api", "fabric-resource-loader-v0", "0.10.8+12a6ba2c17"), ModDependencyFlag.COMPILE, ModDependencyFlag.RUNTIME);
			d.addMaven(FabricMaven.URL, new MavenId(FabricMaven.GROUP_ID + ".fabric-api", "fabric-block-view-api-v2", "1.0.0+73761d2e99"), ModDependencyFlag.COMPILE, ModDependencyFlag.RUNTIME);

			if (CUSTOM_SODIUM) {
				d.add(new JavaJarDependency(getProjectDir().resolve("custom_sodium").resolve(customSodiumName).toAbsolutePath(), null, new MavenId("me.jellysquid.mods", "sodium-fabric", customSodiumName.replace("sodium-fabric-", ""))), ModDependencyFlag.COMPILE, ModDependencyFlag.RUNTIME);
			} else {
				d.addMaven("https://api.modrinth.com/maven", new MavenId("maven.modrinth", "sodium", "mc1.20.1-0.5.8"), ModDependencyFlag.COMPILE, ModDependencyFlag.RUNTIME);
			}
		} else {
			d.addMaven("https://api.modrinth.com/maven", new MavenId("maven.modrinth", "sodium", "mc1.20.1-0.5.8"), ModDependencyFlag.COMPILE);
		}
	}

	@Override
	public String getMavenGroup() {
		return "net.coderbot.iris_mc" + (MC_VERSION.replace('.', '_'));
	}

	private Path[] getDirs(String subdirectory) {
		List<Path> paths = new ArrayList<>();

		for (String sourceSet : SOURCE_SETS) {
			Path path = getProjectDir().resolve("src").resolve(sourceSet).resolve(subdirectory);

			if (Files.exists(path)) {
				paths.add(path);
			}
		}

		return paths.toArray(new Path[0]);
	}

	@Override
	public Path[] getSrcDirs() {
		return getDirs("java");
	}

	@Override
	public Path[] getResourceDirs() {
		return getDirs("resources");
	}

	private final Lazy<String> computeVersionLazy = new Lazy<>(() -> {
		String baseVersion = super.getVersion().replace("-development-environment", "");

		String build_id = System.getenv("GITHUB_RUN_NUMBER");

		if (Objects.equals(System.getProperty("iris.release", "false"), "true")) {
			// We don't want any suffix if we're doing a publish.
			return baseVersion;
		}

		String commitHash = "";
		boolean isDirty = false;
		try {
			Git git = Git.open(getProjectDir().toFile());
			isDirty = !git.status().call().getUncommittedChanges().isEmpty();
			commitHash = git.getRepository().parseCommit(git.getRepository().resolve(Constants.HEAD).toObjectId()).getName().substring(0, 8);
			git.close();
		} catch (RepositoryNotFoundException e) {
			// User might have downloaded the repository as a zip.
			return baseVersion + "nogit";
		} catch (IOException | GitAPIException e) {
			e.printStackTrace();
		}

		if (build_id != null) {
			return baseVersion + "-build." + build_id + "-" + commitHash;
		} else {
			return baseVersion + "-" + commitHash + (isDirty ? "-dirty" : "");
		}
	});

	@Override
	public String getVersion() {
		return computeVersionLazy.get();
	}

	@Override
	public Path getBuildJarPath() {
		return getBuildLibsDir().resolve(getModId() + "-" + "mc" + MC_VERSION + "-" + getVersion() + ".jar");
	}

	@Override
	public ProcessorChain resourcesProcessingChain() {
		return new ProcessorChain(super.resourcesProcessingChain(), new FmjVersionFixer(this));
	}

	public class IrisFabricModule extends SimpleFabricModule {
		public IrisFabricModule(FabricContext context) {
			super(context);
		}

		private ProcessingSink createHeaderClassFilter(JavaCompilationResult compilation, ProcessingSink finalOutput) {
			List<Path> headerSourcePaths = new ArrayList<>();

			for (String sourceSet : HEADER_SOURCE_SETS) {
				headerSourcePaths.add(getProjectDir().resolve("src").resolve(sourceSet).resolve("java"));
			}

			return (in, id) -> {
				Path srcFile = compilation.getSourceFile(id);

				if (srcFile != null) {
					for (Path headerSourceSet : headerSourcePaths) {
						if (srcFile.startsWith(headerSourceSet)) {
							// Do not write files compiled from the "headers" source set to the final JAR.
							return;
						}
					}
				}

				finalOutput.sink(in, id);
			};
		}

		// Copy of Brachyura's FabricModule#createFabricCompilationResult with a one-line change
		@Override
		protected FabricCompilationResult createFabricCompilationResult() {
			try {
				String mixinOut = "mixinmapout.tiny";
				JavaCompilation compilation0 = new JavaCompilation()
					.addOption(JvmUtil.compileArgs(JvmUtil.CURRENT_JAVA_VERSION, getJavaVersion()))
					.addOption(
						"-AbrachyuraInMap=" + writeMappings4FabricStuff().toAbsolutePath().toString(),
						"-AbrachyuraOutMap=" + mixinOut, // Remaps shadows etc
						"-AbrachyuraInNamespace=" + Namespaces.NAMED,
						"-AbrachyuraOutNamespace=" + Namespaces.INTERMEDIARY,
						"-AoutRefMapFile=" + getModuleName() + "-refmap.json", // Remaps annotations
						"-AdefaultObfuscationEnv=brachyura"
					)
					.addClasspath(context.getCompileDependencies())
					.addSourceDir(getSrcDirs());
				for (BuildModule m : getModuleDependencies()) {
					compilation0.addClasspath(m.compilationOutput.get());
				}
				JavaCompilationResult compilation = compilation0.compile();
				ProcessingSponge compilationOutput = new ProcessingSponge();
				// Iris start: Add header class filter
				compilation.getInputs(createHeaderClassFilter(compilation, compilationOutput));
				// Iris end
				ProcessingEntry mixinMappings = compilationOutput.popEntry(mixinOut);
				MemoryMappingTree mixinMappingsTree = null;
				if (mixinMappings != null) {
					mixinMappingsTree = new MemoryMappingTree();
					try (Reader reader = new InputStreamReader(mixinMappings.in.get())) {
						MappingReader.read(reader, MappingFormat.TINY_2, mixinMappingsTree);
					}
				}
				return new FabricCompilationResult(compilationOutput, compilation, mixinMappingsTree);
			} catch (IOException e) {
				throw Util.sneak(e);
			}
		}
	}
}
