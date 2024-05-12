package net.irisshaders.iris.gui;

import org.jetbrains.annotations.Nullable;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Class used to make interfacing with {@link TinyFileDialogs} easier and asynchronous.
 */
public final class FileDialogUtil {
	private static final ExecutorService FILE_DIALOG_EXECUTOR = Executors.newSingleThreadExecutor();

	private FileDialogUtil() {
	}

	/**
	 * Opens an asynchronous file select dialog window.
	 *
	 * @param dialog      Whether to open a "save" dialog or an "open" dialog
	 * @param title       The title of the dialog window
	 * @param origin      The path that the window should start at
	 * @param filterLabel A label used to describe what file extensions are allowed and their purpose
	 * @param filters     The file extension filters used by the dialog, each formatted as {@code "*.extension"}
	 * @return a {@link CompletableFuture} which is completed once a file is selected or the dialog is cancelled.
	 */
	public static CompletableFuture<Optional<Path>> fileSelectDialog(DialogType dialog, String title, @Nullable Path origin, @Nullable String filterLabel, String... filters) {
		CompletableFuture<Optional<Path>> future = new CompletableFuture<>();

		FILE_DIALOG_EXECUTOR.submit(() -> {
			String result = null;

			try (MemoryStack stack = MemoryStack.stackPush()) {
				PointerBuffer filterBuffer = stack.mallocPointer(filters.length);

				for (String filter : filters) {
					filterBuffer.put(stack.UTF8(filter));
				}
				filterBuffer.flip();

				String path = origin != null ? origin.toAbsolutePath().toString() : null;

				if (dialog == DialogType.SAVE) {
					result = TinyFileDialogs.tinyfd_saveFileDialog(title, path, filterBuffer, filterLabel);
				} else if (dialog == DialogType.OPEN) {
					result = TinyFileDialogs.tinyfd_openFileDialog(title, path, filterBuffer, filterLabel, false);
				}
			}

			future.complete(Optional.ofNullable(result).map(Paths::get));
		});

		return future;
	}

	public enum DialogType {
		SAVE, OPEN
	}
}
