package net.irisshaders.iris.pipeline.transform;

import net.fabricmc.loader.api.FabricLoader;
import net.irisshaders.iris.Iris;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Static class that deals with printing the patched_shader folder.
 */
public class ShaderPrinter {
	private static final Path debugOutDir = FabricLoader.getInstance().getGameDir().resolve("patched_shaders");
	private static boolean outputLocationCleared = false;
	private static int programCounter = 0;

	public static void resetPrintState() {
		outputLocationCleared = false;
		programCounter = 0;
	}

	public static ProgramPrintBuilder printProgram(String name) {
		return new ProgramPrintBuilder(name);
	}

	public static class ProgramPrintBuilder {
		// copy the debug flag if for some reason the debug flag changes during the
		// lifetime of this builder object
		private final boolean isActive = Iris.getIrisConfig().areDebugOptionsEnabled();

		// the prefix is created at instantiation time so that all sources attached to
		// this builder use the same counter prefix
		private final String prefix = isActive ? String.format("%03d_", ++programCounter) : null;

		// the prefix and the sources list aren't created if debug is disabled
		private final List<String> sources = isActive ? new ArrayList<>(PatchShaderType.values().length * 2) : null;

		private String name;
		private boolean done = false; // makes the print function idempotent

		public ProgramPrintBuilder(String name) {
			setName(name);
		}

		public ProgramPrintBuilder setName(String name) {
			this.name = name;
			return this;
		}

		private void addItem(String extension, String content) {
			if (content != null && sources != null) {
				sources.add(prefix + name + extension);
				sources.add(content);
			}
		}

		public ProgramPrintBuilder addSource(PatchShaderType type, String source) {
			if (sources == null) {
				return this;
			}
			addItem(type.extension, source);
			return this;
		}

		public ProgramPrintBuilder addSources(Map<PatchShaderType, String> sources) {
			if (sources == null) {
				return this;
			}
			for (Map.Entry<PatchShaderType, String> entry : sources.entrySet()) {
				addSource(entry.getKey(), entry.getValue());
			}
			return this;
		}

		public ProgramPrintBuilder addJson(String json) {
			if (sources == null) {
				return this;
			}
			addItem(".json", json);
			return this;
		}

		public void print() {
			if (done) {
				return;
			}
			done = true;
			if (isActive) {
				if (!outputLocationCleared) {
					try {
						if (Files.exists(debugOutDir)) {
							try (Stream<Path> stream = Files.list(debugOutDir)) {
								stream.forEach(path -> {
									try {
										Files.delete(path);
									} catch (IOException e) {
										throw new RuntimeException(e);
									}
								});
							}
						}

						Files.createDirectories(debugOutDir);
					} catch (IOException e) {
						Iris.logger.warn("Failed to initialize debug patched shader source location", e);
					}
					outputLocationCleared = true;
				}

				try {
					for (int i = 0; i < sources.size(); i += 2) {
						Files.writeString(debugOutDir.resolve(sources.get(i)), sources.get(i + 1));
					}
				} catch (IOException e) {
					Iris.logger.warn("Failed to write debug patched shader source", e);
				}
			}
		}
	}
}
