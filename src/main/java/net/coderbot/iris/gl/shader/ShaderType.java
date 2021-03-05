// This file is based on code from Sodium by JellySquid, licensed under the LGPLv3 license.

package net.coderbot.iris.gl.shader;

import org.lwjgl.opengl.GL20;

/**
 * An enumeration over the supported OpenGL shader types.
 */
public enum ShaderType {
    VERTEX(GL20.GL_VERTEX_SHADER),
    FRAGMENT(GL20.GL_FRAGMENT_SHADER);

    public final int id;

    ShaderType(int id) {
        this.id = id;
    }
}
