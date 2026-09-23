package net.irisshaders.iris.vulkan;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import net.irisshaders.iris.pipeline.transform.PatchShaderType;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.VkDevice;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

public class VulkanShaderCache implements AutoCloseable {
	private static final int MAX_LIBRARIES = 256;
	private static final int MAX_PIPELINE_LIBRARIES = 512;
	private static final int MAX_SPIRV_BYTES = 64 * 1024 * 1024;
	private final VkDevice device;
	private final Map<StageKey, byte[]> stages = new LinkedHashMap<>(16, 0.75f, true);
	private final Map<StageKey, CompletableFuture<byte[]>> compilingStages = new HashMap<>();
	private final Map<LibraryKey, CompletableFuture<VulkanGraphicsPipeline.ShaderLibrary>> libraries = new LinkedHashMap<>(16, 0.75f, true);
	private final Map<PipelineLibraryKey, CompletableFuture<VulkanGraphicsPipeline.PipelineLibrary>> pipelineLibraries = new LinkedHashMap<>(
		16, 0.75f, true);
	private final Set<CompletableFuture<?>> pending = ConcurrentHashMap.newKeySet();
	private final Set<VulkanShaderCompiler> compilers = ConcurrentHashMap.newKeySet();
	private final ThreadLocal<VulkanShaderCompiler> compiler = ThreadLocal.withInitial(() -> {
		var compiler = new VulkanShaderCompiler();
		this.compilers.add(compiler);
		return compiler;
	});
	private final ExecutorService executor;
	private int spirvBytes;
	private boolean closing;
	private volatile boolean closed;

	public VulkanShaderCache(VkDevice device) {
		this.device = device;
		int processors = Runtime.getRuntime().availableProcessors();
		int threads = Math.clamp(Math.min(8, (processors + 1) / 2), 1, processors);
		this.executor = Executors.newFixedThreadPool(threads, Thread.ofPlatform().daemon().name("Iris shader compiler-", 0).factory());
	}

	public static <T> T await(CompletableFuture<T> future) {
		try {
			return future.join();
		} catch (CompletionException failure) {
			if (failure.getCause() instanceof RuntimeException cause) {
				throw cause;
			}

			if (failure.getCause() instanceof Error cause) {
				throw cause;
			}

			throw failure;
		}
	}

	private static HashCode hash(String source) {
		return Hashing.sha256().hashString(source, StandardCharsets.UTF_8);
	}

	public ByteBuffer compile(String name, PatchShaderType stage, String source) {
		var key = new StageKey(stage, hash(source));
		CompletableFuture<byte[]> future;
		boolean compile;

		synchronized (this) {
			if (this.closed) {
				throw new IllegalStateException("shader cache is closed");
			}

			var cached = this.stages.get(key);

			if (cached != null) {
				return MemoryUtil.memAlloc(cached.length).put(cached).flip();
			}

			future = this.compilingStages.get(key);
			compile = future == null;

			if (compile) {
				future = new CompletableFuture<>();
				this.compilingStages.put(key, future);
			}
		}

		if (!compile) {
			var bytes = await(future);
			return MemoryUtil.memAlloc(bytes.length).put(bytes).flip();
		}

		ByteBuffer spirv = null;

		try {
			spirv = this.compiler.get().compile(name, stage, source);
			var bytes = new byte[spirv.remaining()];
			spirv.get(spirv.position(), bytes);

			synchronized (this) {
				if (bytes.length <= MAX_SPIRV_BYTES) {
					this.stages.put(key, bytes);
					this.spirvBytes += bytes.length;
					var iterator = this.stages.values().iterator();

					while (this.spirvBytes > MAX_SPIRV_BYTES) {
						this.spirvBytes -= iterator.next().length;
						iterator.remove();
					}
				}

				this.compilingStages.remove(key);
				future.complete(bytes);
			}

			return spirv;
		} catch (RuntimeException | Error failure) {
			MemoryUtil.memFree(spirv);

			synchronized (this) {
				this.compilingStages.remove(key);
				future.completeExceptionally(failure);
			}

			throw failure;
		}
	}

	public synchronized CompletableFuture<VulkanGraphicsPipeline.ShaderLibrary> acquireLibrary(String name,
		Map<PatchShaderType, String> sources, VulkanSamplerHeap heap, VulkanResourceSets uniforms, VulkanResourceSets frame) {
		this.requireOpen();
		var hashes = new EnumMap<PatchShaderType, HashCode>(PatchShaderType.class);
		sources.forEach((stage, source) -> hashes.put(stage, hash(source)));
		var key = new LibraryKey(Map.copyOf(hashes), VulkanGraphicsPipeline.LibraryLayout.of(heap, uniforms, frame));
		var future = this.libraries.get(key);

		if (future == null) {
			var created = new CompletableFuture<VulkanGraphicsPipeline.ShaderLibrary>();
			this.libraries.put(key, created);
			this.track(created);
			future = created;
			try {
				this.executor.execute(() -> {
					var spirv = new EnumMap<PatchShaderType, ByteBuffer>(PatchShaderType.class);

					try {
						for (var entry : sources.entrySet()) {
							spirv.put(entry.getKey(), this.compile(name + entry.getKey().extension, entry.getKey(), entry.getValue()));
						}

						var library = new VulkanGraphicsPipeline.ShaderLibrary(this, key.sources(), this.device, heap, uniforms, spirv,
							frame);

						synchronized (this) {
							created.complete(library);
							this.trimLibraries();
						}
					} catch (RuntimeException | Error failure) {
						synchronized (this) {
							this.libraries.remove(key, created);
							created.completeExceptionally(failure);
						}
					} finally {
						spirv.values().forEach(MemoryUtil::memFree);
					}
				});
			} catch (RuntimeException | Error failure) {
				this.libraries.remove(key, created);
				created.completeExceptionally(failure);
				throw failure;
			}
		}

		var retained = future.thenApply(VulkanGraphicsPipeline.ShaderLibrary::retain);
		this.trimLibraries();
		return retained;
	}

	public VulkanGraphicsPipeline.PipelineLibrary acquirePipelineLibrary(Map<PatchShaderType, HashCode> sources,
		VulkanGraphicsPipeline.LibraryLayout layout, Object state, LongSupplier compile) {
		var key = new PipelineLibraryKey(sources, layout, state);
		CompletableFuture<VulkanGraphicsPipeline.PipelineLibrary> future;
		CompletableFuture<VulkanGraphicsPipeline.PipelineLibrary> retained;
		boolean create;

		synchronized (this) {
			if (this.closed) {
				throw new IllegalStateException("shader cache is closed");
			}

			future = this.pipelineLibraries.get(key);
			create = future == null;

			if (create) {
				future = new CompletableFuture<>();
				this.pipelineLibraries.put(key, future);
			}

			retained = future.thenApply(VulkanGraphicsPipeline.PipelineLibrary::retain);
		}

		if (create) {
			try {
				var library = new VulkanGraphicsPipeline.PipelineLibrary(this.device, compile.getAsLong());

				synchronized (this) {
					future.complete(library);
					this.trimPipelineLibraries();
				}
			} catch (RuntimeException | Error failure) {
				synchronized (this) {
					this.pipelineLibraries.remove(key, future);
					future.completeExceptionally(failure);
				}
			}
		}

		return await(retained);
	}

	public <T> CompletableFuture<T> submit(Supplier<T> task) {
		this.requireOpen();
		return this.track(CompletableFuture.supplyAsync(task, this.executor));
	}

	public <T, R> CompletableFuture<R> thenApply(CompletableFuture<T> future, Function<T, R> task) {
		this.requireOpen();
		return this.track(future.thenApplyAsync(task, this.executor));
	}

	private <T> CompletableFuture<T> track(CompletableFuture<T> future) {
		this.pending.add(future);
		future.whenComplete((result, failure) -> {
			if (failure == null) {
				this.pending.remove(future);
			}
		});
		return future;
	}

	public void finishCompilation() {
		var futures = this.pending.toArray(CompletableFuture[]::new);

		try {
			await(CompletableFuture.allOf(futures));
		} finally {
			for (var future : futures) {
				this.pending.remove(future);
			}
		}
	}

	public synchronized void releaseLibrary(VulkanGraphicsPipeline.ShaderLibrary library) {
		library.close();
		this.trimLibraries();
		this.trimPipelineLibraries();
	}

	private void trimLibraries() {
		var iterator = this.libraries.values().iterator();

		while (this.libraries.size() > MAX_LIBRARIES && iterator.hasNext()) {
			var future = iterator.next();
			var library = future.isDone() && !future.isCompletedExceptionally() ? future.getNow(null) : null;

			if (library != null && library.isUnused()) {
				iterator.remove();
				library.close();
			}
		}
	}

	private void trimPipelineLibraries() {
		var iterator = this.pipelineLibraries.values().iterator();

		while (this.pipelineLibraries.size() > MAX_PIPELINE_LIBRARIES && iterator.hasNext()) {
			var future = iterator.next();
			var library = future.isDone() && !future.isCompletedExceptionally() ? future.getNow(null) : null;

			if (library != null && library.isUnused()) {
				iterator.remove();
				library.close();
			}
		}
	}

	private void requireOpen() {
		if (this.closing || this.closed) {
			throw new IllegalStateException("shader cache is closed");
		}
	}

	@Override
	public void close() {
		if (this.closing || this.closed) {
			return;
		}

		this.closing = true;

		try {
			this.finishCompilation();
		} catch (RuntimeException | Error ignored) {
		} finally {
			this.executor.close();
		}

		this.closed = true;
		this.libraries.values().forEach(future -> {
			if (!future.isCompletedExceptionally()) {
				future.join().close();
			}
		});
		this.libraries.clear();
		this.pipelineLibraries.values().forEach(future -> future.join().close());
		this.pipelineLibraries.clear();
		this.stages.clear();
		this.spirvBytes = 0;
		this.compilers.forEach(VulkanShaderCompiler::close);
		this.compilers.clear();
	}

	public record PipelineLibraryKey(Map<PatchShaderType, HashCode> sources, VulkanGraphicsPipeline.LibraryLayout layout, Object state) {
	}

	public record StageKey(PatchShaderType stage, HashCode source) {
	}

	public record LibraryKey(Map<PatchShaderType, HashCode> sources, VulkanGraphicsPipeline.LibraryLayout layout) {
	}
}
