// This file is based on code from Sodium by JellySquid, licensed under the LGPLv3 license.

package net.coderbot.iris.gl.shader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import com.mojang.blaze3d.platform.GlStateManager;
import net.coderbot.iris.gl.GlObject;
import net.coderbot.iris.gl.IrisRenderSystem;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL20C;

/**
 * A compiled OpenGL shader object.
 */
public class Shader extends GlObject {
	private static final Logger LOGGER = LogManager.getLogger(Shader.class);

	public Shader(ShaderType type, String src, ShaderConstants constants) {
		src = Shader.processShader(src, constants);

		int handle = GL20C.glCreateShader(type.getId());
		ShaderWorkarounds.safeShaderSource(handle, src);
		GL20C.glCompileShader(handle);

		String log = IrisRenderSystem.getShaderInfoLog(handle);

		if (!log.isEmpty()) {
			LOGGER.warn("Shader compilation log: " + log);
		}

		int result = GlStateManager.glGetShaderi(handle, GL20C.GL_COMPILE_STATUS);

		if (result != GL20C.GL_TRUE) {
			throw new RuntimeException("Shader compilation failed, see log for details");
		}

		this.setHandle(handle);
	}

	/**
	 * Adds an additional list of defines to the top of a GLSL shader file just after the version declaration. This
	 * allows for ghetto shader specialization.
	 */
	public static String processShader(String src, ShaderConstants constants) {
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
					for (String macro : constants.getDefineStrings()) {
						builder.append(macro).append('\n');
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

	public void delete() {
		GlStateManager.glDeleteShader(getHandle());

		this.invalidateHandle();
	}
}
