// This file is based on ShaderType from Sodium by JellySquid, licensed under the LGPLv3 license.

package net.coderbot.iris.gl.buffer;

import org.lwjgl.opengl.GL43C;

/**
 * An enumeration over the supported OpenGL buffer types.
 */
public enum BufferType {
	SSBO(GL43C.GL_SHADER_STORAGE_BUFFER),
	UBO(GL43C.GL_UNIFORM_BUFFER);

	public final int id;

	BufferType(int id) {
		this.id = id;
	}

	public static BufferType parse(String name) {
		switch (name) {
			case "ubo": return UBO;
			case "ssbo": return SSBO;
			default: throw new IllegalArgumentException("Unknown buffer type: " + name);
		}
	}
}
