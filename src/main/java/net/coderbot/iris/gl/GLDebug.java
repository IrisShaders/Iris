/*
 * Copyright LWJGL. All rights reserved. Modified by IMS for use in Iris (net.coderbot.iris.gl).
 * License terms: https://www.lwjgl.org/license
 */

package net.coderbot.iris.gl;

import org.lwjgl.opengl.AMDDebugOutput;
import org.lwjgl.opengl.ARBDebugOutput;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL43C;
import org.lwjgl.opengl.GLCapabilities;
import org.lwjgl.opengl.GLDebugMessageAMDCallback;
import org.lwjgl.opengl.GLDebugMessageARBCallback;
import org.lwjgl.opengl.GLDebugMessageCallback;
import org.lwjgl.opengl.KHRDebug;
import org.lwjgl.system.APIUtil;
import org.lwjgl.system.Callback;

import javax.annotation.Nullable;
import java.io.PrintStream;
import java.util.function.Consumer;

public final class GLDebug {
	@Nullable
	public static Callback setupDebugMessageCallback() {
		return setupDebugMessageCallback(APIUtil.DEBUG_STREAM);
	}

	private static void trace(Consumer<String> output) {
		/*
		 * We can not just use a fixed stacktrace element offset, because some methods
		 * are intercepted and some are not. So, check the package name.
		 */
		StackTraceElement[] elems = filterStackTrace(new Throwable(), 4).getStackTrace();
		for (StackTraceElement ste : elems) {
			output.accept(ste.toString());
		}
	}

	public static Throwable filterStackTrace(Throwable throwable, int offset) {
		StackTraceElement[] elems = throwable.getStackTrace();
		StackTraceElement[] filtered = new StackTraceElement[elems.length];
		int j = 0;
		for (int i = offset; i < elems.length; i++) {
			String className = elems[i].getClassName();
			if (className == null) {
				className = "";
			}
			if (className.startsWith("org.lwjglx.") && !className.startsWith("org.lwjglx.debug.opengl")
					&& !className.startsWith("org.lwjglx.debug.glfw")) {
				continue;
			}
			filtered[j++] = elems[i];
		}
		StackTraceElement[] newElems = new StackTraceElement[j];
		System.arraycopy(filtered, 0, newElems, 0, j);
		throwable.setStackTrace(newElems);
		return throwable;
	}

	private static void printTrace(PrintStream stream) {
		trace(new Consumer<String>() {
			boolean first = true;

			public void accept(String str) {
				if (first) {
					printDetail(stream, "Stacktrace", str);
					first = false;
				} else {
					printDetailLine(stream, "Stacktrace", str);
				}
			}
		});
	}

	@Nullable
	public static Callback setupDebugMessageCallback(PrintStream stream) {
		GLCapabilities caps = GL.getCapabilities();
		if (caps.OpenGL43) {
			APIUtil.apiLog("[GL] Using OpenGL 4.3 for error logging.");
			GLDebugMessageCallback proc = GLDebugMessageCallback.create((source, type, id, severity, length, message, userParam) -> {
				stream.println("[LWJGL] OpenGL debug message");
				printDetail(stream, "ID", String.format("0x%X", id));
				printDetail(stream, "Source", getDebugSource(source));
				printDetail(stream, "Type", getDebugType(type));
				printDetail(stream, "Severity", getDebugSeverity(severity));
				printDetail(stream, "Message", GLDebugMessageCallback.getMessage(length, message));
				printTrace(stream);
			});
			GL43C.glDebugMessageCallback(proc, 0L);
			if ((GL43C.glGetInteger(33310) & 2) == 0) {
				APIUtil.apiLog("[GL] Warning: A non-debug context may not produce any debug output.");
				GL43C.glEnable(37600);
			}

			return proc;
		} else if (caps.GL_KHR_debug) {
			APIUtil.apiLog("[GL] Using KHR_debug for error logging.");
			GLDebugMessageCallback proc = GLDebugMessageCallback.create((source, type, id, severity, length, message, userParam) -> {
				stream.println("[LWJGL] OpenGL debug message");
				printDetail(stream, "ID", String.format("0x%X", id));
				printDetail(stream, "Source", getDebugSource(source));
				printDetail(stream, "Type", getDebugType(type));
				printDetail(stream, "Severity", getDebugSeverity(severity));
				printDetail(stream, "Message", GLDebugMessageCallback.getMessage(length, message));
				printTrace(stream);
			});
			KHRDebug.glDebugMessageCallback(proc, 0L);
			if (caps.OpenGL30 && (GL43C.glGetInteger(33310) & 2) == 0) {
				APIUtil.apiLog("[GL] Warning: A non-debug context may not produce any debug output.");
				GL43C.glEnable(37600);
			}

			return proc;
		} else if (caps.GL_ARB_debug_output) {
			APIUtil.apiLog("[GL] Using ARB_debug_output for error logging.");
			GLDebugMessageARBCallback proc = GLDebugMessageARBCallback.create((source, type, id, severity, length, message, userParam) -> {
				stream.println("[LWJGL] ARB_debug_output message");
				printDetail(stream, "ID", String.format("0x%X", id));
				printDetail(stream, "Source", getSourceARB(source));
				printDetail(stream, "Type", getTypeARB(type));
				printDetail(stream, "Severity", getSeverityARB(severity));
				printDetail(stream, "Message", GLDebugMessageARBCallback.getMessage(length, message));
				printTrace(stream);
			});
			ARBDebugOutput.glDebugMessageCallbackARB(proc, 0L);
			return proc;
		} else if (caps.GL_AMD_debug_output) {
			APIUtil.apiLog("[GL] Using AMD_debug_output for error logging.");
			GLDebugMessageAMDCallback proc = GLDebugMessageAMDCallback.create((id, category, severity, length, message, userParam) -> {
				stream.println("[LWJGL] AMD_debug_output message");
				printDetail(stream, "ID", String.format("0x%X", id));
				printDetail(stream, "Category", getCategoryAMD(category));
				printDetail(stream, "Severity", getSeverityAMD(severity));
				printDetail(stream, "Message", GLDebugMessageAMDCallback.getMessage(length, message));
				printTrace(stream);
			});
			AMDDebugOutput.glDebugMessageCallbackAMD(proc, 0L);
			return proc;
		} else {
			APIUtil.apiLog("[GL] No debug output implementation is available.");
			return null;
		}
	}

	private static void printDetail(PrintStream stream, String type, String message) {
		stream.printf("\t%s: %s\n", type, message);
	}

	private static void printDetailLine(PrintStream stream, String type, String message) {
		stream.append("    ");
		for (int i = 0; i < type.length(); i++) {
			stream.append(" ");
		}
		stream.append(message).append("\n");
	}

	private static String getDebugSource(int source) {
		switch(source) {
			case 33350:
				return "API";
			case 33351:
				return "WINDOW SYSTEM";
			case 33352:
				return "SHADER COMPILER";
			case 33353:
				return "THIRD PARTY";
			case 33354:
				return "APPLICATION";
			case 33355:
				return "OTHER";
			default:
				return APIUtil.apiUnknownToken(source);
		}
	}

	private static String getDebugType(int type) {
		switch(type) {
			case 33356:
				return "ERROR";
			case 33357:
				return "DEPRECATED BEHAVIOR";
			case 33358:
				return "UNDEFINED BEHAVIOR";
			case 33359:
				return "PORTABILITY";
			case 33360:
				return "PERFORMANCE";
			case 33361:
				return "OTHER";
			case 33384:
				return "MARKER";
			default:
				return APIUtil.apiUnknownToken(type);
		}
	}

	private static String getDebugSeverity(int severity) {
		switch(severity) {
			case 33387:
				return "NOTIFICATION";
			case 37190:
				return "HIGH";
			case 37191:
				return "MEDIUM";
			case 37192:
				return "LOW";
			default:
				return APIUtil.apiUnknownToken(severity);
		}
	}

	private static String getSourceARB(int source) {
		switch(source) {
			case 33350:
				return "API";
			case 33351:
				return "WINDOW SYSTEM";
			case 33352:
				return "SHADER COMPILER";
			case 33353:
				return "THIRD PARTY";
			case 33354:
				return "APPLICATION";
			case 33355:
				return "OTHER";
			default:
				return APIUtil.apiUnknownToken(source);
		}
	}

	private static String getTypeARB(int type) {
		switch(type) {
			case 33356:
				return "ERROR";
			case 33357:
				return "DEPRECATED BEHAVIOR";
			case 33358:
				return "UNDEFINED BEHAVIOR";
			case 33359:
				return "PORTABILITY";
			case 33360:
				return "PERFORMANCE";
			case 33361:
				return "OTHER";
			default:
				return APIUtil.apiUnknownToken(type);
		}
	}

	private static String getSeverityARB(int severity) {
		switch(severity) {
			case 37190:
				return "HIGH";
			case 37191:
				return "MEDIUM";
			case 37192:
				return "LOW";
			default:
				return APIUtil.apiUnknownToken(severity);
		}
	}

	private static String getCategoryAMD(int category) {
		switch(category) {
			case 37193:
				return "API ERROR";
			case 37194:
				return "WINDOW SYSTEM";
			case 37195:
				return "DEPRECATION";
			case 37196:
				return "UNDEFINED BEHAVIOR";
			case 37197:
				return "PERFORMANCE";
			case 37198:
				return "SHADER COMPILER";
			case 37199:
				return "APPLICATION";
			case 37200:
				return "OTHER";
			default:
				return APIUtil.apiUnknownToken(category);
		}
	}

	private static String getSeverityAMD(int severity) {
		switch(severity) {
			case 37190:
				return "HIGH";
			case 37191:
				return "MEDIUM";
			case 37192:
				return "LOW";
			default:
				return APIUtil.apiUnknownToken(severity);
		}
	}
}
