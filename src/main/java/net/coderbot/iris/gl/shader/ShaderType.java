// This file is based on code from Sodium by JellySquid, licensed under the LGPLv3 license.

package net.coderbot.iris.gl.shader;

import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL32C;

/**
 * An enumeration over the supported OpenGL shader types.
 */
public enum ShaderType {
	VERTEX(GL20.GL_VERTEX_SHADER, ".vsh"),
	GEOMETRY(GL32C.GL_GEOMETRY_SHADER, ".gsh"),
	FRAGMENT(GL20.GL_FRAGMENT_SHADER, ".fsh");

	private final int id;
	private final String extension;

	ShaderType(int id, String extension) {
		this.id = id;
		this.extension = extension;
	}

	public int getId() {
		return this.id;
	}

	public String getExtension() {
		return this.extension;
	}
}
