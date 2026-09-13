package net.irisshaders.iris.gl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintStream;
import java.util.ArrayDeque;
import java.util.Arrays;

public final class GLDebug {
	private static final Logger LOGGER = LoggerFactory.getLogger("Iris");
	private static final ArrayDeque<String> groups = new ArrayDeque<>();

	private GLDebug() {
	}

	public static int setupDebugMessageCallback() {
		return setupDebugMessageCallback(System.out);
	}

	public static int setupDebugMessageCallback(PrintStream stream) {
		reloadDebugState();

		return 0;
	}

	public static int disableDebugMessages() {
		groups.clear();

		return 0;
	}

	public static void reloadDebugState() {
		groups.clear();
	}

	public static void nameObject(int type, int object, String name) {
		LOGGER.trace("Iris resource {}", name);
	}

	public static void pushGroup(int id, String name) {
		groups.push(name);
		LOGGER.trace("Iris pass {}", name);
	}

	public static void popGroup() {
		if (!groups.isEmpty()) {
			groups.pop();
		}
	}

	public static Throwable filterStackTrace(Throwable throwable, int offset) {
		var trace = throwable.getStackTrace();
		throwable.setStackTrace(Arrays.copyOfRange(trace, Math.min(offset, trace.length), trace.length));

		return throwable;
	}
}
