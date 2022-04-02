import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.FileSystem;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import io.github.coolcrabs.brachyura.dependency.JavaJarDependency;
import io.github.coolcrabs.brachyura.fabric.FabricLoader;
import io.github.coolcrabs.brachyura.fabric.FabricMaven;
import io.github.coolcrabs.brachyura.mappings.Namespaces;
import io.github.coolcrabs.brachyura.maven.Maven;
import io.github.coolcrabs.brachyura.maven.MavenId;
import io.github.coolcrabs.brachyura.minecraft.Minecraft;
import io.github.coolcrabs.brachyura.minecraft.VersionMeta;
import io.github.coolcrabs.brachyura.processing.ProcessorChain;
import io.github.coolcrabs.brachyura.util.AtomicFile;
import io.github.coolcrabs.brachyura.util.FileSystemUtil;
import io.github.coolcrabs.brachyura.util.PathUtil;
import net.fabricmc.accesswidener.AccessWidenerReader;
import net.fabricmc.accesswidener.AccessWidenerVisitor;
import net.fabricmc.mappingio.tree.MappingTree;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Constants;

public class Buildscript extends MultiSrcDirFabricProject {
	static final boolean SODIUM = true;
	static final boolean CUSTOM_SODIUM = true;
	static final String customSodiumName = "sodium-fabric-0.5.0mc1.18.1beta7-unstable.jar";

	@Override
	public VersionMeta createMcVersion() {
		return Minecraft.getVersion("1.18.1");
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
		d.addMaven(Maven.MAVEN_CENTRAL, new MavenId("org.anarres:jcpp:1.4.14"), ModDependencyFlag.COMPILE, ModDependencyFlag.RUNTIME, ModDependencyFlag.JIJ);
		d.addMaven(FabricMaven.URL, new MavenId(FabricMaven.GROUP_ID + ".fabric-api", "fabric-resource-loader-v0", "0.4.11+3ac43d9565"), ModDependencyFlag.COMPILE, ModDependencyFlag.RUNTIME, ModDependencyFlag.JIJ);
		d.addMaven(FabricMaven.URL, new MavenId(FabricMaven.GROUP_ID + ".fabric-api", "fabric-key-binding-api-v1", "1.0.8+c8aba2f365"), ModDependencyFlag.COMPILE, ModDependencyFlag.RUNTIME, ModDependencyFlag.JIJ);
		d.addMaven(FabricMaven.URL, new MavenId(FabricMaven.GROUP_ID + ".fabric-api", "fabric-api-base", "0.4.1+b4f4f6cd65"), ModDependencyFlag.COMPILE, ModDependencyFlag.RUNTIME);
		d.addMaven(FabricMaven.URL, new MavenId(FabricMaven.GROUP_ID + ".fabric-api", "fabric-rendering-data-attachment-v1", "0.3.4+7242e9d765"), ModDependencyFlag.COMPILE, ModDependencyFlag.RUNTIME);
		d.addMaven(FabricMaven.URL, new MavenId(FabricMaven.GROUP_ID + ".fabric-api", "fabric-rendering-fluids-v1", "0.1.18+3ac43d9565"), ModDependencyFlag.COMPILE, ModDependencyFlag.RUNTIME);

		if (SODIUM) {
			JavaJarDependency sodium;
			if (CUSTOM_SODIUM) {
				sodium = new JavaJarDependency(getProjectDir().resolve("custom_sodium").resolve(customSodiumName).toAbsolutePath(), null, new MavenId("net.caffeinemc", "sodium-fabric", customSodiumName.replace("sodium-fabric-", "").replace(".jar", "")));
			} else {
				sodium = Maven.getMavenJarDep("https://api.modrinth.com/maven", new MavenId("maven.modrinth", "sodium", "mc1.18.2-0.4.1"));
			}
			d.add(sodium, ModDependencyFlag.COMPILE, ModDependencyFlag.RUNTIME);
			try {
				try (FileSystem sfs = FileSystemUtil.newJarFileSystem(sodium.jar)) {
					Files.walkFileTree(sfs.getPath("META-INF/jars"), new SimpleFileVisitor<Path>(){
						@Override
						public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
							String fileName = file.getFileName().toString();
							if (fileName.endsWith(".jar")) {
								boolean shouldUse = false;
								String name = null;
								String version = null;
								if (fileName.startsWith("sodium-gfx-opengl-")) {
									shouldUse = true;
									name = "sodium-gfx-opengl";
									version = fileName.replace("sodium-gfx-opengl-", "").replace(".jar", "");
								} else if (fileName.startsWith("sodium-gfx-")) {
									shouldUse = true;
									name = "sodium-gfx";
									version = fileName.replace("sodium-gfx-", "").replace(".jar", "");
								}
								if (shouldUse) {
									Path p = PathUtil.resolveAndCreateDir(getLocalBrachyuraPath(), "sodiumlibs").resolve(name + "-" + version + ".jar");
									boolean unstable = version.endsWith("unstable"); 
									if (unstable || !Files.exists(p)) {
										try (AtomicFile f = new AtomicFile(p)) {
											Files.copy(file, f.tempPath, StandardCopyOption.REPLACE_EXISTING);
											f.commit();
										}
									}
									d.add(new JavaJarDependency(p, null, unstable ? null : new MavenId("net.caffeinemc", name, version)), ModDependencyFlag.COMPILE, ModDependencyFlag.RUNTIME);
								}
							}
							return super.visitFile(file, attrs);
						}
					});
				}
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}

		d.addMaven(Maven.MAVEN_CENTRAL, new MavenId("org.joml:joml:1.10.2"), ModDependencyFlag.COMPILE, ModDependencyFlag.RUNTIME);
	}

	@Override
	public Path[] paths(String subdir, boolean onlyHeaders) {
		List<Path> r = new ArrayList<>();
		if (!onlyHeaders) {
			Collections.addAll(
				r,
				getProjectDir().resolve("src").resolve("main").resolve(subdir),
				getProjectDir().resolve("src").resolve("vendored").resolve(subdir)
			);
			if (SODIUM) {
				r.add(getProjectDir().resolve("src").resolve("sodiumCompatibility").resolve(subdir));
			} else {
				r.add(getProjectDir().resolve("src").resolve("noSodiumStub").resolve(subdir));
			}
		}
		r.add(getProjectDir().resolve("src").resolve("headers").resolve(subdir));
		r.removeIf(p -> !Files.exists(p));
		return r.toArray(new Path[0]);
	}

	@Override
	public String getVersion() {
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
		} catch (IOException | GitAPIException e) {
			e.printStackTrace();
		}

		return baseVersion + commitHash + (isDirty ? "-dirty" : "");
	}

	@Override
	public Path getBuildJarPath() {
		return getBuildLibsDir().resolve(getModId() + "-" + "mc" + createMcVersion().version + "-" + getVersion() + ".jar");
	}

	@Override
	public ProcessorChain resourcesProcessingChain() {
		return new ProcessorChain(super.resourcesProcessingChain(), new FmjVersionFixer(this));
	}
}
