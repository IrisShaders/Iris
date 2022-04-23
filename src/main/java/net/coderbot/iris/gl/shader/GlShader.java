// This file is based on code from Sodium by JellySquid, licensed under the LGPLv3 license.

package net.coderbot.iris.gl.shader;

import com.mojang.blaze3d.platform.GlStateManager;
import net.coderbot.iris.gl.GlResource;
import net.coderbot.iris.gl.IrisRenderSystem;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL20C;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Collections;

/**
 * A compiled OpenGL shader object.
 */
public class GlShader extends GlResource {
	private static final Logger LOGGER = LogManager.getLogger(GlShader.class);

	private final String name;

	public GlShader(ShaderType type, String name, String src, Iterable<String> defines) {
		super(createShader(type, name, src, defines));

		this.name = name;
	}

	public GlShader(ShaderType type, String name, String src) {
		this(type, name, src, Collections.emptyList());
	}

	private static int createShader(ShaderType type, String name, String src, Iterable<String> defines) {
		src = processShader(src, defines);

		int handle = GlStateManager.glCreateShader(type.id);
		ShaderWorkarounds.safeShaderSource(handle, src);
		GlStateManager.glCompileShader(handle);

		String log = IrisRenderSystem.getShaderInfoLog(handle);

		if (!log.isEmpty()) {
			LOGGER.warn("Shader compilation log for " + name + ": " + log);
		}

		int result = GlStateManager.glGetShaderi(handle, GL20C.GL_COMPILE_STATUS);

		if (result != GL20C.GL_TRUE) {
			throw new RuntimeException("Shader compilation failed, see log for details");
		}

		return handle;
	}

	/**
	 * Adds an additional list of defines to the top of a GLSL shader file just after the version declaration. This
	 * allows for ghetto shader specialization.
	 */
	public static String processShader(String src, Iterable<String> defines) {
		StringBuilder builder = new StringBuilder(src.length());
		boolean patched = false;

		try (BufferedReader reader = new BufferedReader(new StringReader(src))) {
			String line;

			while ((line = reader.readLine()) != null) {
				// Write the line out to the patched GLSL code string
				builder.append(line).append("\n");

				// Now, see if the line we just wrote declares the version
				// If we haven't already added our define declarations, add them just after the version declaration
				if (!patched && line.startsWith("#version")) {
					for (String define : defines) {
						builder.append(define).append('\n');
					}

					// We did our work, don't add them again
					patched = true;
				}
			}
		} catch (IOException e) {
			throw new RuntimeException("Could not process shader source", e);
		}

		return builder.toString();
	}

	public String getName() {
		return this.name;
	}

	public int getHandle() {
		return this.getGlId();
	}

	@Override
	protected void destroyInternal() {
		GlStateManager.glDeleteShader(this.getGlId());
	}
}
