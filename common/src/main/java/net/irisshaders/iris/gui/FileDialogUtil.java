package net.irisshaders.iris.gui;

import org.jetbrains.annotations.Nullable;
import org.lwjgl.PointerBuffer;
import org.lwjgl.sdl.SDLDialog;
import org.lwjgl.sdl.SDLProperties;
import org.lwjgl.sdl.SDL_DialogFileCallbackI;
import org.lwjgl.sdl.SDL_DialogFileFilter;
import org.lwjgl.system.MemoryUtil;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.lwjgl.sdl.SDLDialog.*;
import static org.lwjgl.sdl.SDLProperties.*;

public final class FileDialogUtil {
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
	public static CompletableFuture<Optional<Path>> fileSelectDialog(
		DialogType dialog,
		String title,
		@Nullable Path origin,
		@Nullable String filterLabel,
		String... filters
	) {
		CompletableFuture<Optional<Path>> future = new CompletableFuture<>();

		/*
		 * SDL requires the filters and their strings to remain valid until
		 * the callback is invoked, so these must not be stack-allocated.
		 */
		SDL_DialogFileFilter.Buffer filterBuffer =
			filters.length == 0 ? null : SDL_DialogFileFilter.calloc(filters.length);

		if (filterBuffer != null) {
			for (int i = 0; i < filters.length; i++) {
				String pattern = toSDLFilter(filters[i]);

				filterBuffer.get(i)
					.name(MemoryUtil.memUTF8(
						filterLabel != null ? filterLabel : filters[i]
					))
					.pattern(MemoryUtil.memUTF8(pattern));
			}
		}

		int properties = SDL_CreateProperties();

		if (properties == 0) {
			freeFilters(filterBuffer);
			future.completeExceptionally(
				new IllegalStateException("Failed to create SDL dialog properties")
			);
			return future;
		}

		SDL_SetStringProperty(
			properties,
			SDL_PROP_FILE_DIALOG_TITLE_STRING,
			title
		);

		if (origin != null) {
			SDL_SetStringProperty(
				properties,
				SDL_PROP_FILE_DIALOG_LOCATION_STRING,
				origin.toAbsolutePath().toString()
			);
		}

		if (filterBuffer != null) {
			SDL_SetPointerProperty(
				properties,
				SDL_PROP_FILE_DIALOG_FILTERS_POINTER,
				filterBuffer.address()
			);

			SDL_SetNumberProperty(
				properties,
				SDL_PROP_FILE_DIALOG_NFILTERS_NUMBER,
				filterBuffer.remaining()
			);
		}

		SDL_DialogFileCallbackI callback = (userdata, fileList, selectedFilter) -> {
			try {
				if (fileList == MemoryUtil.NULL) {
					future.completeExceptionally(
						new IllegalStateException("SDL file dialog failed")
					);
					return;
				}

				PointerBuffer files = MemoryUtil.memPointerBuffer(fileList, 1);
				long firstFile = files.get(0);

				if (firstFile == MemoryUtil.NULL) {
					future.complete(Optional.empty());
				} else {
					String path = MemoryUtil.memUTF8(firstFile);
					future.complete(Optional.of(Paths.get(path)));
				}
			} catch (Throwable t) {
				future.completeExceptionally(t);
			} finally {
				SDL_DestroyProperties(properties);
				freeFilters(filterBuffer);
			}
		};

		int type = switch (dialog) {
			case OPEN -> SDL_FILEDIALOG_OPENFILE;
			case SAVE -> SDL_FILEDIALOG_SAVEFILE;
		};

		SDL_ShowFileDialogWithProperties(
			type,
			callback,
			MemoryUtil.NULL,
			properties
		);

		return future;
	}

	private static String toSDLFilter(String filter) {
		/*
		 * tinyfiledialogs uses patterns like "*.zip".
		 * SDL expects just "zip", or semicolon-separated extensions
		 * such as "zip;jar".
		 */
		if (filter.equals("*") || filter.equals("*.*")) {
			return "*";
		}

		if (filter.startsWith("*.")) {
			return filter.substring(2);
		}

		return filter;
	}

	private static void freeFilters(@Nullable SDL_DialogFileFilter.Buffer filters) {
		if (filters == null) {
			return;
		}

		for (int i = 0; i < filters.capacity(); i++) {
			SDL_DialogFileFilter filter = filters.get(i);

			MemoryUtil.memFree(filter.name());
			MemoryUtil.memFree(filter.pattern());
		}

		filters.free();
	}

	public enum DialogType {
		SAVE,
		OPEN
	}
}
