import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import io.github.coolcrabs.accesswidener.AccessWidener;
import io.github.coolcrabs.brachyura.compiler.java.JavaCompilation;
import io.github.coolcrabs.brachyura.compiler.java.JavaCompilationResult;
import io.github.coolcrabs.brachyura.decompiler.BrachyuraDecompiler;
import io.github.coolcrabs.brachyura.dependency.JavaJarDependency;
import io.github.coolcrabs.brachyura.fabric.FabricContext;
import io.github.coolcrabs.brachyura.fabric.FabricLoader;
import io.github.coolcrabs.brachyura.fabric.FabricMaven;
import io.github.coolcrabs.brachyura.fabric.FabricModule;
import io.github.coolcrabs.brachyura.fabric.Intermediary;
import io.github.coolcrabs.brachyura.fabric.SimpleFabricProject;
import io.github.coolcrabs.brachyura.fabric.FabricContext.ModDependencyCollector;
import io.github.coolcrabs.brachyura.fabric.FabricContext.ModDependencyFlag;
import io.github.coolcrabs.brachyura.mappings.MappingHasher;
import io.github.coolcrabs.brachyura.mappings.MappingHelper;
import io.github.coolcrabs.brachyura.mappings.Namespaces;
import io.github.coolcrabs.brachyura.mappings.tinyremapper.MappingTreeMappingProvider;
import io.github.coolcrabs.brachyura.mappings.tinyremapper.TinyRemapperHelper;
import io.github.coolcrabs.brachyura.mappings.tinyremapper.TrWrapper;
import io.github.coolcrabs.brachyura.maven.Maven;
import io.github.coolcrabs.brachyura.maven.MavenId;
import io.github.coolcrabs.brachyura.minecraft.Minecraft;
import io.github.coolcrabs.brachyura.minecraft.VersionMeta;
import io.github.coolcrabs.brachyura.processing.ProcessingEntry;
import io.github.coolcrabs.brachyura.processing.ProcessingSink;
import io.github.coolcrabs.brachyura.processing.ProcessingSource;
import io.github.coolcrabs.brachyura.processing.ProcessorChain;
import io.github.coolcrabs.brachyura.processing.sinks.ZipProcessingSink;
import io.github.coolcrabs.brachyura.processing.sources.ProcessingSponge;
import io.github.coolcrabs.brachyura.processing.sources.ZipProcessingSource;
import io.github.coolcrabs.brachyura.project.java.BuildModule;
import io.github.coolcrabs.brachyura.util.AtomicDirectory;
import io.github.coolcrabs.brachyura.util.AtomicFile;
import io.github.coolcrabs.brachyura.util.CloseableArrayList;
import io.github.coolcrabs.brachyura.util.JvmUtil;
import io.github.coolcrabs.brachyura.util.Lazy;
import io.github.coolcrabs.brachyura.util.MessageDigestUtil;
import io.github.coolcrabs.brachyura.util.PathUtil;
import io.github.coolcrabs.brachyura.util.Util;
import io.github.coolmineman.trieharder.FindReplaceSourceRemapper;
import net.fabricmc.mappingio.MappingReader;
import net.fabricmc.mappingio.format.MappingFormat;
import net.fabricmc.mappingio.tree.MappingTree;
import net.fabricmc.mappingio.tree.MemoryMappingTree;
import net.fabricmc.tinyremapper.TinyRemapper;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.RepositoryNotFoundException;
import org.eclipse.jgit.lib.Constants;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;

public class Buildscript extends SimpleFabricProject {
	static final boolean SODIUM = true;
	static final boolean CUSTOM_SODIUM = false;
	static final String MC_VERSION = "22w42a";
	static final String customSodiumName = "sodium-fabric-mc1.19.2-0.4.3+rev.653869b.jar";

	private static final String[] SOURCE_SETS = new String[] {
		"main",
		"vendored",
		SODIUM ? "sodiumCompatibility" : "noSodiumStub",
		"headers"
	};

	public class IrisFabricContext extends FabricContext {
		@Override
		public VersionMeta createMcVersion() {
			return Buildscript.this.createMcVersion();
		}

		@Override
		public MappingTree createMappings() {
			return Buildscript.this.createMappings();
		}

		@Override
		public FabricLoader getLoader() {
			return Buildscript.this.getLoader();
		}

		@Override
		public void getModDependencies(ModDependencyCollector d) {
			Buildscript.this.getModDependencies(d);
		}

		@Override
		protected @Nullable AccessWidener createAw() {
			return Buildscript.this.createAw();
		}

		@Override
		public @Nullable BrachyuraDecompiler decompiler() {
			return Buildscript.this.decompiler();
		}

		@Override
		protected MappingTree createIntermediary() {
			return Intermediary.ofMaven(FabricMaven.URL, FabricMaven.intermediary("1.19.2")).tree;
		}


		@Override
		protected RemappedJar createIntermediaryJar() {
			Path mergedJar = getMergedJar();
			String intermediaryHash = MappingHasher.hashSha256(intermediary.get());
			return new RemappedJar(mergedJar, intermediaryHash);
		}

		@Override
		protected List<ModDependency> createRemappedModDependencies() {
			class RemapInfo {
				ModDependency source;
				ModDependency target;
			}
			try {
				List<ModDependency> unmapped = modDependencies.get();
				if (unmapped == null || unmapped.isEmpty()) return Collections.emptyList();
				List<RemapInfo> remapinfo = new ArrayList<>(unmapped.size());
				List<ModDependency> remapped = new ArrayList<>(unmapped.size());
				MessageDigest dephasher = MessageDigestUtil.messageDigest(MessageDigestUtil.SHA256);
				dephasher.update(remappedModsLogicVersion()); // Bump this if the logic changes
				for (ModDependency dep : unmapped) {
					hashDep(dephasher, dep);
				}
				for (JavaJarDependency dep : mcClasspath.get()) {
					hashDep(dephasher, dep);
				}
				dephasher.update(namedJar.get().mappingHash.getBytes(StandardCharsets.UTF_8));
				MessageDigestUtil.update(dephasher, TinyRemapperHelper.VERSION);
				String dephash = MessageDigestUtil.toHexHash(dephasher.digest());
				Path depdir = remappedModsRootPath();
				Path resultdir = depdir.resolve(dephash);
				for (ModDependency u : unmapped) {
					RemapInfo ri = new RemapInfo();
					remapinfo.add(ri);
					ri.source = u;
					remapped.add(ri.source);
				}
				return remapped;
			} catch (Exception e) {
				throw Util.sneak(e);
			}
		}
		@Override
		public MappingTree createMojmap() {
			return createMojmap(versionMeta.get());
		}

		public MappingTree createMojmap(VersionMeta meta) {
			try {
				MemoryMappingTree r = new MemoryMappingTree(true);
				Minecraft.getMojmap(meta).accept(r);
				MappingHelper.dropNullInNamespace(r, Namespaces.INTERMEDIARY);
				return r;
			} catch (IOException e) {
				throw Util.sneak(e);
			}
		}

		@Override
		public Path getContextRoot() {
			return getProjectDir();
		}
	}

	@Override
	protected FabricContext createContext() {
		return new IrisFabricContext();
	}

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
		return new FabricLoader(FabricMaven.URL, FabricMaven.loader("0.14.8"));
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
	public void getModDependencies(ModDependencyCollector d) {
		jij(d.addMaven(Maven.MAVEN_CENTRAL, new MavenId("org.anarres:jcpp:1.4.14"), ModDependencyFlag.COMPILE, ModDependencyFlag.RUNTIME));
		jij(d.addMaven(FabricMaven.URL, new MavenId(FabricMaven.GROUP_ID + ".fabric-api", "fabric-key-binding-api-v1", "1.0.20+aeb40ebe90"), ModDependencyFlag.COMPILE, ModDependencyFlag.RUNTIME));

		jij(d.addMaven(Maven.MAVEN_CENTRAL, new MavenId("io.github.douira:glsl-transformer:1.0.1"), ModDependencyFlag.COMPILE, ModDependencyFlag.RUNTIME));
		jij(d.addMaven(Maven.MAVEN_CENTRAL, new MavenId("org.antlr:antlr4-runtime:4.10.1"), ModDependencyFlag.COMPILE, ModDependencyFlag.RUNTIME));

		if (SODIUM) {
			d.addMaven(FabricMaven.URL, new MavenId(FabricMaven.GROUP_ID + ".fabric-api", "fabric-api-base", "0.4.3+d7c144a8d2"), ModDependencyFlag.COMPILE, ModDependencyFlag.RUNTIME);
			d.addMaven(FabricMaven.URL, new MavenId(FabricMaven.GROUP_ID + ".fabric-api", "fabric-rendering-data-attachment-v1", "0.3.8+d7c144a8a7"), ModDependencyFlag.COMPILE, ModDependencyFlag.RUNTIME);
			d.addMaven(FabricMaven.URL, new MavenId(FabricMaven.GROUP_ID + ".fabric-api", "fabric-rendering-fluids-v1", "3.0.0+56447d9ba7"), ModDependencyFlag.COMPILE, ModDependencyFlag.RUNTIME);
			d.addMaven(FabricMaven.URL, new MavenId(FabricMaven.GROUP_ID + ".fabric-api", "fabric-resource-loader-v0", "0.7.0+93d8cb8290"), ModDependencyFlag.COMPILE, ModDependencyFlag.RUNTIME);

			if (CUSTOM_SODIUM) {
				d.add(new JavaJarDependency(getProjectDir().resolve("custom_sodium").resolve(customSodiumName).toAbsolutePath(), null, new MavenId("me.jellysquid.mods", "sodium-fabric", customSodiumName.replace("sodium-fabric-", ""))), ModDependencyFlag.COMPILE, ModDependencyFlag.RUNTIME);
			} else {
				d.addMaven("https://api.modrinth.com/maven", new MavenId("maven.modrinth", "sodium", "mc1.19.2-0.4.4"), ModDependencyFlag.COMPILE, ModDependencyFlag.RUNTIME);
			}
		} else {
			d.addMaven("https://api.modrinth.com/maven", new MavenId("maven.modrinth", "sodium", "mc1.19.2-0.4.4"), ModDependencyFlag.COMPILE);
		}

		d.addMaven(Maven.MAVEN_CENTRAL, new MavenId("org.joml:joml:1.10.2"), ModDependencyFlag.COMPILE, ModDependencyFlag.RUNTIME);
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
						"-AbrachyuraOutNamespace=" + Namespaces.NAMED,
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
