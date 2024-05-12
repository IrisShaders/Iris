// This file is based on code from Sodium by JellySquid, licensed under the LGPLv3 license.

package net.irisshaders.iris.gl.shader;

import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL32C;
import org.lwjgl.opengl.GL43C;

/**
 * An enumeration over the supported OpenGL shader types.
 */
public enum ShaderType {
	VERTEX(GL20.GL_VERTEX_SHADER),
	GEOMETRY(GL32C.GL_GEOMETRY_SHADER),
	FRAGMENT(GL20.GL_FRAGMENT_SHADER),
	COMPUTE(GL43C.GL_COMPUTE_SHADER),
	TESSELATION_CONTROL(GL43C.GL_TESS_CONTROL_SHADER),
	TESSELATION_EVAL(GL43C.GL_TESS_EVALUATION_SHADER);

	public final int id;

	ShaderType(int id) {
		this.id = id;
	}
}
