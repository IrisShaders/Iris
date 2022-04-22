import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import io.github.coolcrabs.brachyura.compiler.java.JavaCompilation;
import io.github.coolcrabs.brachyura.compiler.java.JavaCompilationResult;
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
import io.github.coolcrabs.brachyura.util.JvmUtil;
import io.github.coolcrabs.brachyura.util.Lazy;
import io.github.coolcrabs.brachyura.util.Util;
import net.fabricmc.accesswidener.AccessWidenerReader;
import net.fabricmc.accesswidener.AccessWidenerVisitor;
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
	static final String customSodiumName = "sodium-fabric-mc1.18.2-0.4.1+rev.d50338a.jar";

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
		return Minecraft.getVersion("1.18.2");
	}

	@Override
	public MappingTree createMappings() {
		return createMojmap();
	}

	@Override
	public FabricLoader getLoader() {
		return new FabricLoader(FabricMaven.URL, FabricMaven.loader("0.13.3"));
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
    public Consumer<AccessWidenerVisitor> getAw() {
        return v -> {
            try {
                new AccessWidenerReader(v).read(Files.newBufferedReader(getResourcesDir().resolve("iris.accesswidener")), Namespaces.NAMED);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        };
    }

    @Override
    public void getModDependencies(ModDependencyCollector d) {
		jij(d.addMaven(Maven.MAVEN_CENTRAL, new MavenId("org.anarres:jcpp:1.4.14"), ModDependencyFlag.COMPILE, ModDependencyFlag.RUNTIME));
		jij(d.addMaven(FabricMaven.URL, new MavenId(FabricMaven.GROUP_ID + ".fabric-api", "fabric-resource-loader-v0", "0.4.16+55dca1a4d2"), ModDependencyFlag.COMPILE, ModDependencyFlag.RUNTIME));
		jij(d.addMaven(FabricMaven.URL, new MavenId(FabricMaven.GROUP_ID + ".fabric-api", "fabric-key-binding-api-v1", "1.0.11+54e5b2ecd2"), ModDependencyFlag.COMPILE, ModDependencyFlag.RUNTIME));
		jij(d.addMaven(FabricMaven.URL, new MavenId(FabricMaven.GROUP_ID + ".fabric-api", "fabric-command-api-v1", "1.1.9+d7c144a860"), ModDependencyFlag.COMPILE, ModDependencyFlag.RUNTIME));
		jij(d.addMaven(FabricMaven.URL, new MavenId(FabricMaven.GROUP_ID + ".fabric-api", "fabric-api-base", "0.4.3+d7c144a8d2"), ModDependencyFlag.COMPILE, ModDependencyFlag.RUNTIME));

		d.addMaven(FabricMaven.URL, new MavenId(FabricMaven.GROUP_ID + ".fabric-api", "fabric-rendering-data-attachment-v1", "0.3.6+d7c144a8d2"), ModDependencyFlag.COMPILE, ModDependencyFlag.RUNTIME);
		d.addMaven(FabricMaven.URL, new MavenId(FabricMaven.GROUP_ID + ".fabric-api", "fabric-rendering-fluids-v1", "2.0.1+54e5b2ecd2"), ModDependencyFlag.COMPILE, ModDependencyFlag.RUNTIME);

		if (SODIUM) {
			if (CUSTOM_SODIUM) {
				d.add(new JavaJarDependency(getProjectDir().resolve("custom_sodium").resolve(customSodiumName).toAbsolutePath(), null, new MavenId("me.jellysquid.mods", "sodium-fabric", customSodiumName.replace("sodium-fabric-", ""))), ModDependencyFlag.COMPILE, ModDependencyFlag.RUNTIME);
			} else {
				d.addMaven("https://api.modrinth.com/maven", new MavenId("maven.modrinth", "sodium", "mc1.18.2-0.4.1"), ModDependencyFlag.COMPILE, ModDependencyFlag.RUNTIME);
			}
		} else {
			d.addMaven("https://api.modrinth.com/maven", new MavenId("maven.modrinth", "sodium", "mc1.18.2-0.4.1"), ModDependencyFlag.COMPILE);
		}

		d.addMaven(Maven.MAVEN_CENTRAL, new MavenId("org.joml:joml:1.10.2"), ModDependencyFlag.COMPILE, ModDependencyFlag.RUNTIME);
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
		String baseVersion = super.getVersion().replace("development-environment", "");

		String build_id = System.getenv("GITHUB_RUN_NUMBER");

		if (build_id != null) {
			return baseVersion + "build." + build_id;
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

		return baseVersion + commitHash + (isDirty ? "-dirty" : "");
	});

	@Override
	public String getVersion() {
		return computeVersionLazy.get();
	}

	@Override
	public Path getBuildJarPath() {
		return getBuildLibsDir().resolve(getModId() + "-" + "mc" + createMcVersion().version + "-" + getVersion() + ".jar");
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
