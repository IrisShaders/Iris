package net.irisshaders.iris.backend;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.Supplier;

public class CompileTask<T> extends CompletableFuture<T> implements Runnable {
	private final ExecutorService executor;
	private Supplier<T> compile;
	private boolean started;

	public CompileTask(Supplier<T> compile, ExecutorService executor) {
		this.compile = compile;
		this.executor = executor;
	}

	public static <T> T await(CompletableFuture<T> future) {
		if (future instanceof CompileTask<T> task) {
			task.run();
		}
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

	public void schedule(CompletableFuture<?> input) {
		input.whenComplete((result, failure) -> {
			synchronized (this) {
				if (this.started) {
					return;
				}
				if (failure != null) {
					this.started = true;
					this.compile = null;
					this.completeExceptionally(failure);
				} else {
					try {
						this.executor.execute(this);
					} catch (RuntimeException failureToSchedule) {
						this.started = true;
						this.compile = null;
						this.completeExceptionally(failureToSchedule);
					}
				}
			}
		});
	}

	@Override
	public void run() {
		Supplier<T> compile;
		synchronized (this) {
			if (this.started) {
				return;
			}
			this.started = true;
			compile = this.compile;
			this.compile = null;
			this.remove();
		}
		try {
			this.complete(compile.get());
		} catch (Throwable failure) {
			this.completeExceptionally(failure);
		}
	}

	@Override
	public synchronized boolean cancel(boolean interrupt) {
		if (this.started) {
			return false;
		}
		this.started = true;
		this.compile = null;
		this.remove();
		return super.cancel(false);
	}

	private void remove() {
		if (this.executor instanceof ThreadPoolExecutor pool) {
			pool.remove(this);
		}
	}
}
