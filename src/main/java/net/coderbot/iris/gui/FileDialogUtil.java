package net.coderbot.iris.gui;

import org.jetbrains.annotations.Nullable;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

/**
 * Class used to make interfacing with {@link TinyFileDialogs} easier.
 */
public final class FileDialogUtil {
	private FileDialogUtil() {}

	/**
	 * Opens a file select dialog window.
	 *
	 * <p>Will stall the thread that the method is invoked within.
	 *
	 * @param dialog Whether to open a "save" dialog or an "open" dialog
	 * @param title The title of the dialog window
	 * @param origin The path that the window should start at
	 * @param filters The file extension filters used by the dialog, each formatted as {@code "*.extension"}
	 * @param filterDesc A message used to describe the file extensions used as filters
	 * @return a path to the file selected by the user, unless the dialog has been canceled.
	 */
	public static Optional<Path> fileSelectDialog(DialogType dialog, String title, @Nullable Path origin, String[] filters, @Nullable String filterDesc) {
		String result = null;

		try (MemoryStack stack = MemoryStack.stackPush()) {
			PointerBuffer filterBuffer = stack.mallocPointer(filters.length);

			for (String filter : filters) {
				filterBuffer.put(stack.UTF8(filter));
			}
			filterBuffer.flip();

			String path = origin != null ? origin.toAbsolutePath().toString() : null;

			if (dialog == DialogType.SAVE) {
				result = TinyFileDialogs.tinyfd_saveFileDialog(title, path, filterBuffer, filterDesc);
			} else if (dialog == DialogType.OPEN) {
				result = TinyFileDialogs.tinyfd_openFileDialog(title, path, filterBuffer, filterDesc, false);
			}
		}

		return Optional.ofNullable(result).map(Paths::get);
	}

	public enum DialogType {
		SAVE, OPEN;
	}
}
